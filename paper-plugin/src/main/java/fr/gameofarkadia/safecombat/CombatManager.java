package fr.gameofarkadia.safecombat;

import fr.gameofarkadia.safecombat.listener.task.PlayerFightingTask;
import fr.gameofarkadia.safecombat.listener.task.PlayerProtectedTask;
import fr.gameofarkadia.safecombat.util.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Manages which player is fighting with who.
 */
public class CombatManager implements SafeCombatManager {

  private final Map<UUID, PlayerFightingTask> fightingPlayers = new HashMap<>();
  private final Map<UUID, PlayerProtectedTask> protectedPlayers = new HashMap<>();
  private final Set<UUID> playersToKill = new HashSet<>();

  /**
   * Get the players currently in fight.
   *
   * @return a non-mutable collection.
   */
  public @UnmodifiableView Map<UUID, PlayerFightingTask> getFightingPlayers() {
    return fightingPlayers;
  }

  /**
   * Get the players to be killed.
   *
   * @return a non-mutable collection.
   */
  public @UnmodifiableView Collection<UUID> getPlayersToKill() {
    return Collections.unmodifiableCollection(playersToKill);
  }

  /**
   * Add a player to be killed.
   * @param player target player.
   */
  public void addPlayerToKill(@NotNull OfflinePlayer player) {
    Main.getInstance().getSLF4JLogger().info("Player ADDED to to-kill list: {}.", player.getUniqueId());
    playersToKill.add(player.getUniqueId());
  }

  public void removePlayerToKill(@NotNull OfflinePlayer player) {
    Main.getInstance().getSLF4JLogger().info("Player REMOVED from to-kill list: {}.", player.getUniqueId());
    playersToKill.remove(player.getUniqueId());
  }

  /**
   * Get the players currently protected.
   *
   * @return a non-mutable map.
   */
  public @UnmodifiableView Map<UUID, PlayerProtectedTask> getProtectedPlayers() {
    return Collections.unmodifiableMap(protectedPlayers);
  }

  /**
   * Make the player fighting : run the task and show the messages & titles
   *
   * @param player The player
   */
  public void setPlayerFighting(@NotNull Player player) {
    fightingPlayers.put(player.getUniqueId(), new PlayerFightingTask(player));
    player.sendMessage(Util.prefix() + Main.getLang().get("fight.enter"));
  }

  /**
   * Update starting instant (if the player is damaged again)
   *
   * @param player The player
   */
  public void updateInstant(@NotNull OfflinePlayer player) {
    fightingPlayers.get(player.getUniqueId()).setStartingInstant(Instant.now());
  }

  @Override
  public boolean isFighting(@NotNull UUID playerUUID) {
    return fightingPlayers.containsKey(playerUUID);
  }

  @Override
  public boolean isProtected(@NotNull UUID playerUUID) {
    return protectedPlayers.containsKey(playerUUID);
  }

  @Override
  public void addPlayerProtection(@NotNull OfflinePlayer player, @NotNull Duration duration) {
    Instant end = Instant.now().plus(duration);
    protectedPlayers.put(player.getUniqueId(), new PlayerProtectedTask(player, end));
  }

  @Override
  public boolean removePlayerProtection(@NotNull OfflinePlayer player) {
    var task = protectedPlayers.remove(player.getUniqueId());
    if (task != null) {
      task.cancel();
      return true;
    }
    return false;
  }

  public boolean removeFromFighting(@NotNull OfflinePlayer player) {
    var task = fightingPlayers.remove(player.getUniqueId());
    if(task != null) {
      task.cancel();
      return true;
    }
    return false;
  }
}