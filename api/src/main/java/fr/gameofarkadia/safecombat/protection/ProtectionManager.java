package fr.gameofarkadia.safecombat.protection;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * Interface to manage "new player protection".
 */
public interface ProtectionManager {

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
   * @return {@code true} if the player was protected and is now unprotected, {@code false} otherwise.
   */
  boolean removePlayerProtection(@NotNull OfflinePlayer player);

  void signalPlayerJoined(@NotNull Player player);

  void reloadFromDatabaseAsync();

}
