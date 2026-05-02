package fr.gameofarkadia.safecombat.protection;

import fr.gameofarkadia.arkadialib.api.database.DatabaseManager;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.listener.task.PlayerProtectedTask;
import fr.gameofarkadia.safecombat.sync.SyncCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProtectionManagerImpl implements ProtectionManager {

  private final Map<UUID, PlayerProtectedTask> localProtectionTasks = new ConcurrentHashMap<>();
  private final Map<UUID, ProtectedData> protections = new ConcurrentHashMap<>();
  private final PlayerProtectionDatabase database;

  public ProtectionManagerImpl(@NotNull DatabaseManager manager) {
    database = new PlayerProtectionDatabase(manager);
  }

  @Override
  public boolean isProtected(@NotNull UUID playerUUID) {
    return protections.containsKey(playerUUID);
  }

  @Override
  public void addPlayerProtection(@NotNull OfflinePlayer player, @NotNull ProtectionReason reason, @NotNull Duration duration) {
    //TODO if protected, add duration.
    if(isProtected(player)) return;

    var data = ProtectedData.generate(player, duration);
    protections.put(player.getUniqueId(), data);
    localProtectionTasks.put(player.getUniqueId(), new PlayerProtectedTask(data));
    Main.logger().info("Adding player protection: {}.", data);

    // We do not persist SERVER_JOIN
    if(reason != ProtectionReason.SERVER_JOIN)
      SafeCombatScheduler.runAsync(() -> database.insert(data));
  }

  @Override
  public boolean removePlayerProtection(@NotNull UUID uuid) {
    var data = protections.remove(uuid);
    if(data == null) return false;

    SafeCombatScheduler.runAsync(() -> database.delete(data));
    Optional.ofNullable(localProtectionTasks.remove(uuid))
        .ifPresent(PlayerProtectedTask::cancel);

    // propagate
    Main.synchronizer().sendRpc(SyncCommand.REMOVED_PROTECTION, uuid);

    return true;
  }

  @Override
  public @NotNull Duration getRemainingDuration(@NotNull UUID uuid) {
    return Optional.ofNullable(protections.get(uuid))
        .map(ProtectedData::duration)
        .orElse(Duration.ZERO);
  }

  @Override
  public void playerProtectionRemovedByRemote(@NotNull UUID uuid) {
    Optional.ofNullable(localProtectionTasks.remove(uuid))
        .ifPresent(PlayerProtectedTask::cancel);
    protections.remove(uuid);
  }

  @Override
  public CompletableFuture<Void> signalPlayerJoined(@NotNull Player player) {
    return SafeCombatScheduler.execAsync(() -> {
      UUID uuid = player.getUniqueId();
      var data = database.getProtectionEntry(uuid);
      if(data == null) return;

      // Register and start local task.
      protections.put(uuid, data);
      var task = localProtectionTasks.get(uuid);
      if(task != null) {
        player.sendMessage(Main.prefix() + "§6Rappel : §eVous bénéficiez d'une protection. Vous ne pouvez§c ni attaquer, ni être attaqué§e. Pour y renoncer, faites la commande §c/protection disable§e.");
        task.updatePlayer(player);
      }
    });
  }

  @Override
  public void signalPlayerLeft(@NotNull Player player) {
    UUID uuid = player.getUniqueId();
    protections.remove(uuid);
    var task = localProtectionTasks.remove(uuid);
    if(task != null) {
      task.cancel();
    }
  }

  @Override
  public void recompute() {
    // Clear all
    protections.clear();
    localProtectionTasks.values().forEach(PlayerProtectedTask::cancel);
    localProtectionTasks.clear();

    // Recompute for all players
    Bukkit.getOnlinePlayers().forEach(this::signalPlayerJoined);
  }
}
