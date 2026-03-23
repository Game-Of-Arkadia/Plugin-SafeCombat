package fr.gameofarkadia.safecombat.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * Interface to manage the combat and protection of players.<br/>
 * Used for the API, to allow other plugins to check if a player is fighting or protected.
 */
public interface SafeCombatManager {

  /**
   * Check if player is fighting.
   * @param player The player.
   * @return {@code true} if the player is in a fight.
   */
  default boolean isFighting(@NotNull OfflinePlayer player) {
    return isFighting(player.getUniqueId());
  }

  /**
   * Check if player is fighting.
   * @param playerUUID The player UUID.
   * @return {@code true} if the player is in a fight.
   */
  boolean isFighting(@NotNull UUID playerUUID);

  /**
   * Check if player is protected.
   * @param player The player.
   * @return {@code true} if the player cannot be attacked.
   */
  default boolean isProtected(@NotNull OfflinePlayer player) {
    return isProtected(player.getUniqueId());
  }

  /**
   * Check if player is protected.
   * @param playerUUID The player UUID.
   * @return {@code true} if the player cannot be attacked.
   */
  boolean isProtected(@NotNull UUID playerUUID);

  /**
   * Protect a player for a duration.
   * @param player the player to protect.
   * @param duration the duration.
   */
  void addPlayerProtection(@NotNull OfflinePlayer player, @NotNull Duration duration);

  /**
   * Cancel a protection.
   * @param player the player to cancel the protection of.
   */
  void removePlayerProtection(@NotNull OfflinePlayer player);

}
