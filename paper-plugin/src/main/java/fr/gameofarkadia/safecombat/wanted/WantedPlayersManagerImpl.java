package fr.gameofarkadia.safecombat.wanted;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.listener.task.PlayerLocalWantedTask;
import fr.gameofarkadia.safecombat.listener.task.PlayerRemoteWantedTask;
import fr.gameofarkadia.safecombat.sync.SyncCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WantedPlayersManagerImpl implements WantedPlayersManager {

  private final Map<UUID, PlayerLocalWantedTask> localTasks = new ConcurrentHashMap<>();
  private final Map<UUID, PlayerRemoteWantedTask> remoteTasks = new ConcurrentHashMap<>();
  private final Map<UUID, WantedPlayer> wantedPlayers = new ConcurrentHashMap<>();

  @Override
  public boolean isWanted(@NotNull UUID uuid) {
    return wantedPlayers.containsKey(uuid);
  }

  @Override
  public boolean isWantedLocally(@NotNull UUID player) {
    return localTasks.containsKey(player);
  }

  @Override
  public @NotNull WantedPlayer getWanted(@NotNull OfflinePlayer player) throws NoSuchElementException {
    return Optional.ofNullable(wantedPlayers.get(player.getUniqueId())).orElseThrow();
  }

  // -- set

  @Override
  public void declareWanted(@NotNull Player player) {
    UUID uuid = player.getUniqueId();
    if(isWanted(player.getUniqueId())) {
      Main.logger().warn("Player {} has been declared has wanted... But already is !", player);
      return;
    }

    WantedPlayer data = WantedPlayer.of(player);
    wantedPlayers.put(uuid, data);
    localTasks.put(uuid, new PlayerLocalWantedTask(player));

    // Propagate to network
    Main.synchronizer().sendRpc(SyncCommand.WANTED_NEW, data.uuid(), data.serverId(), data.timestamp());
  }

  @Override
  public void receivedRemoveWanted(@NotNull WantedPlayer data) {
    if(isWanted(data.uuid())) {
      Main.logger().warn("Received {} from network, but player is already wanted. Ignoring.", data);
      return;
    }

    // Start lookup task.
    wantedPlayers.put(data.uuid(), data);
    remoteTasks.put(data.uuid(), new PlayerRemoteWantedTask(data));
  }

  // -- clear

  @Override
  public void clearLocalWanted(@NotNull UUID uuid) {
    if(!isWanted(uuid)) {
      Main.logger().warn("Player with UUID {} is not wanted, but clearLocalWanted was called. Ignoring.", uuid);
      return;
    }

    wantedPlayers.remove(uuid);
    Optional.ofNullable(localTasks.remove(uuid)).ifPresent(PlayerLocalWantedTask::cancel);

    // Drop request on network
    Main.synchronizer().sendRpc(SyncCommand.WANTED_CLEAR, uuid);
  }

  @Override
  public void clearRemoteWanted(@NotNull UUID uuid) {
    if(!isWanted(uuid)) {
      Main.logger().warn("Player with UUID {} is not wanted, but CLEAR request was called. Ignoring.", uuid);
      return;
    }

    wantedPlayers.remove(uuid);
    Optional.ofNullable(remoteTasks.remove(uuid)).ifPresent(PlayerRemoteWantedTask::cancel);
  }

  @Override
  public void reconnected(@NotNull Player player) {
    clearLocalWanted(player.getUniqueId());
    Bukkit.broadcast(Component.text(Main.prefix() + "§7Le joueur §e" + player.getName() + "§7 s'est reconnecté à temps."));
    SafeCombatAPI.getCombatManager().refreshFight(player);
  }

}
