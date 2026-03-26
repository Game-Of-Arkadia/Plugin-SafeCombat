package fr.gameofarkadia.safecombat.storage;

import com.google.common.base.Preconditions;
import fr.gameofarkadia.arkadialib.api.database.DatabaseManager;
import fr.gameofarkadia.arkadialib.api.utils.DurationWrapper;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatManager;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.listener.task.PlayerDisconnectedTask;
import fr.gameofarkadia.safecombat.listener.task.PlayerFightingTask;
import fr.gameofarkadia.safecombat.listener.task.PlayerProtectedTask;
import fr.gameofarkadia.safecombat.sync.SyncEvent;
import fr.gameofarkadia.safecombat.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages <b>local</b> state. Replicated across servers.
 */
public class PlayerStatesManager implements SafeCombatManager {

  // Local data
  private final Set<UUID> cachedKnownPlayers = ConcurrentHashMap.newKeySet();
  private final Map<UUID, PlayerDisconnectedTask> localDisconnectTasks = new ConcurrentHashMap<>();
  private final Map<UUID, PlayerProtectedTask> localProtectionTasks = new ConcurrentHashMap<>();
  private final Map<UUID, PlayerFightingTask> localFightingPlayers = new HashMap<>();

  // Synchronized data
  private final Map<UUID, ToKillData> killOrders = new ConcurrentHashMap<>();
  private final Map<UUID, ProtectedData> protections = new ConcurrentHashMap<>();
  private final PlayerStatesDatabase database;

  public PlayerStatesManager(@NotNull DatabaseManager manager) {
    database = new PlayerStatesDatabase(manager);
    initialLoad();
  }

  private void initialLoad() {
    SafeCombatScheduler.execAsync(() -> {
      database.getKillOrders().forEach(data -> killOrders.put(data.player(), data));
      database.getProtectionEntries().forEach(data -> {
        protections.put(data.player(), data);
        localProtectionTasks.put(data.player(), new PlayerProtectedTask(Bukkit.getOfflinePlayer(data.player()), data));
      });
    }).exceptionally(err -> {
      Main.logger().error("Could not load player states from database.", err);
      return null;
    });
  }

  /**
   * Make the player fighting : run the task and show the messages & titles
   *
   * @param player The player.
   * @return true if the player is now fighting, false if he was already fighting and his timer has been refreshed.
   */
  public boolean playerIsFighting(@NotNull Player player) {
    UUID uuid = player.getUniqueId();
    if(isFighting(uuid)) {
      localFightingPlayers.get(uuid).refresh();
      return false;
    } else {
      localFightingPlayers.put(uuid, new PlayerFightingTask(player));
      player.sendMessage(Util.prefix() + Main.getLang().get("fight.enter"));
      return true;
    }
  }

  public boolean removeFromFighting(@NotNull OfflinePlayer player) {
    var task = localFightingPlayers.remove(player.getUniqueId());
    if(task != null) {
      task.cancel();
      return true;
    }
    return false;
  }

  public void startPlayerDisconnectTask(@NotNull Player player, @NotNull DurationWrapper duration) {
    if(localDisconnectTasks.containsKey(player.getUniqueId())) {
      Main.logger().error("Start player disconnect task when a task already exists ! Player = {}/{}.", player.getName(), player.getUniqueId());
    }
    localDisconnectTasks.put(player.getUniqueId(), new PlayerDisconnectedTask(player, duration));
  }

  @Override
  public boolean isFighting(@NotNull UUID playerUUID) {
    return localFightingPlayers.containsKey(playerUUID);
  }

  @Override
  public boolean shouldBeKilled(@NotNull UUID playerUUID) {
    return killOrders.containsKey(playerUUID);
  }

  @Override
  public boolean isProtected(@NotNull UUID playerUUID) {
    return protections.containsKey(playerUUID);
  }

  @Override
  public void addPlayerProtection(@NotNull OfflinePlayer player, @NotNull Duration duration) {
    if(isProtected(player)) return;
    var data = ProtectedData.generate(player, duration);
    protections.put(player.getUniqueId(), data);
    localProtectionTasks.put(player.getUniqueId(), new PlayerProtectedTask(player, data));
    SafeCombatScheduler.runAsync(() -> database.insert(data));
  }

  @Override
  public boolean removePlayerProtection(@NotNull OfflinePlayer player) {
    var data = protections.remove(player.getUniqueId());
    if(data == null) return false;
    SafeCombatScheduler.runAsync(() -> database.delete(data));
    Optional.ofNullable(localProtectionTasks.remove(player.getUniqueId())).ifPresent(PlayerProtectedTask::cancel);
    return true;
  }

  public @NotNull PlayerProtectedTask getProtectedTask(@NotNull OfflinePlayer player) {
    return Objects.requireNonNull(localProtectionTasks.get(player.getUniqueId()), "Check if a layer is protected before fetching it !");
  }

  /**
   * Register kill order.
   * @param player new player.
   * @param originalDisconnectTime disconnect-time.
   */
  public void addPlayerKillOrder(@NotNull Player player, @NotNull LocalDateTime originalDisconnectTime) {
    if(shouldBeKilled(player)) return;

    Main.synchronizer().getServerId(id -> {
      var data = ToKillData.generate(player, id, originalDisconnectTime);
      killOrders.put(player.getUniqueId(), data);
      SafeCombatScheduler.runAsync(() -> database.insert(data));
    });
  }

  public void playerHasBeenPunished(@NotNull Player player) {
    var data = killOrders.remove(player.getUniqueId());
    if(data == null) return;

    SafeCombatScheduler.execAsync(() -> database.delete(data)).whenComplete((x, err) -> {
      if(err != null) {
        Main.logger().error("Could not delete kill-order of player {} after punishing him. He may be punished again if he reconnects.", player.getName(), err);
      } else {
        Main.synchronizer().publish(SyncEvent.PLAYER_RECONNECT_PUNISHED, player.getUniqueId());
      }
    });
  }

  public boolean wasDisconnected(@NotNull Player player) {
    Preconditions.checkState(!shouldBeKilled(player), "Check if should be killed first!");
    if(localDisconnectTasks.containsKey(player.getUniqueId())) {
      localDisconnectTasks.get(player.getUniqueId()).playerJoined();
      return true;
    }
    return false;
  }

  public @NotNull CompletableFuture<Boolean> isItFirstConnection(@NotNull Player player) {
    if(cachedKnownPlayers.contains(player.getUniqueId())) {
      return CompletableFuture.completedFuture(false);
    }
    return SafeCombatScheduler.execAsync(() -> {
      boolean isFirstConnection = database.isItFirstPlayerConnection(player);
      if (!isFirstConnection) {
        cachedKnownPlayers.add(player.getUniqueId());
      }
      return isFirstConnection;
    }).orTimeout(5, TimeUnit.SECONDS);
  }
}
