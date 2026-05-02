package fr.gameofarkadia.safecombat.protection;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
  void addPlayerProtection(@NotNull OfflinePlayer player, @NotNull ProtectionReason reason, @NotNull Duration duration);

  /**
   * Cancel a protection.
   * @param player the player to cancel the protection of.
   * @return {@code true} if the player was protected and is now unprotected, {@code false} otherwise.
   */
  default boolean removePlayerProtection(@NotNull OfflinePlayer player) {
    return removePlayerProtection(player.getUniqueId());
  }

  boolean removePlayerProtection(@NotNull UUID uuid);

  default @NotNull Duration getRemainingDuration(@NotNull OfflinePlayer player) {
    return getRemainingDuration(player.getUniqueId());
  }

  @NotNull Duration getRemainingDuration(@NotNull UUID uuid);

  /**
   * Only with remote.
   * @param uuid UUID of the player.
   */
  @ApiStatus.Internal
  void playerProtectionRemovedByRemote(@NotNull UUID uuid);

  @ApiStatus.Internal
  CompletableFuture<Void> signalPlayerJoined(@NotNull Player player);

  @ApiStatus.Internal
  void signalPlayerLeft(@NotNull Player player);

  @ApiStatus.Internal
  void recompute();

}
