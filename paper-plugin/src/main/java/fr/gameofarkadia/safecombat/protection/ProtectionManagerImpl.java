package fr.gameofarkadia.safecombat.protection;

import fr.gameofarkadia.arkadialib.api.database.DatabaseManager;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.listener.task.PlayerProtectedTask;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProtectionManagerImpl implements ProtectionManager {

  private final Map<UUID, PlayerProtectedTask> localProtectionTasks = new ConcurrentHashMap<>();
  private final Map<UUID, ProtectedData> protections = new ConcurrentHashMap<>();
  private final PlayerProtectionDatabase database;

  public ProtectionManagerImpl(@NotNull DatabaseManager manager) {
    database = new PlayerProtectionDatabase(manager);
    initialLoad();
  }

  private void initialLoad() {
    SafeCombatScheduler.execAsync(() -> {
      database.getProtectionEntries().forEach(data -> {
        protections.put(data.player(), data);
        localProtectionTasks.put(data.player(), new PlayerProtectedTask(data));
      });
    }).exceptionally(err -> {
      Main.logger().error("Could not load player protections from database.", err);
      return null;
    });
  }

  @Override
  public void reloadFromDatabaseAsync() {
    protections.clear();
    localProtectionTasks.values().forEach(PlayerProtectedTask::cancel);
    localProtectionTasks.clear();
    initialLoad();
  }

  @Override
  public boolean isProtected(@NotNull UUID playerUUID) {
    return protections.containsKey(playerUUID);
  }

  @Override
  public void addPlayerProtection(@NotNull OfflinePlayer player, @NotNull Duration duration) {
    //TODO if protected, add duration.
    if(isProtected(player)) return;

    var data = ProtectedData.generate(player, duration);
    protections.put(player.getUniqueId(), data);
    localProtectionTasks.put(player.getUniqueId(), new PlayerProtectedTask(data));
    SafeCombatScheduler.runAsync(() -> database.insert(data));
  }

  @Override
  public boolean removePlayerProtection(@NotNull OfflinePlayer player) {
    var data = protections.remove(player.getUniqueId());
    if(data == null) return false;
    SafeCombatScheduler.runAsync(() -> database.delete(data));
    Optional.ofNullable(localProtectionTasks.remove(player.getUniqueId()))
        .ifPresent(PlayerProtectedTask::cancel);
    return true;
  }

  @Override
  public void signalPlayerJoined(@NotNull Player player) {
    var task = localProtectionTasks.get(player.getUniqueId());
    if(task != null) {
      task.updatePlayer(player);
    }
  }
}
