package fr.gameofarkadia.safecombat;

import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * Plugin entry-point.
 */
public final class SafeCombatAPI {
  private SafeCombatAPI() {}

  private static SafeCombatManager instance;

  @ApiStatus.Internal
  static void initialize(@NotNull SafeCombatManager plugin) {
    Preconditions.checkState(instance == null, "SafeCombatAPI is already initialized");
    instance = plugin;
  }

  /**
   * Check if player is fighting.
   * @param playerUUID The player UUID.
   * @return {@code true} if the player is in a fight.
   */
  public static boolean isFighting(@NotNull UUID playerUUID) {
    return instance.isFighting(playerUUID);
  }

  /**
   * Check if player is fighting.
   * @param player The player to check.
   * @return {@code true} if the player is in a fight.
   */
  public static boolean  isFighting(@NotNull OfflinePlayer player) {
    return instance.isFighting(player);
  }

  /**
   * Check if player is protected.
   * @param playerUUID The player UUID.
   * @return {@code true} if the player cannot be attacked.
   */
  public static boolean isProtected(@NotNull UUID playerUUID) {
    return instance.isProtected(playerUUID);
  }

  /**
   * Check if player is protected.
   * @param player The player to check.
   * @return {@code true} if the player cannot be attacked.
   */
  public static boolean isProtected(@NotNull OfflinePlayer player) {
    return instance.isProtected(player);
  }

  /**
   * Protect a player for a duration.
   * @param player the player to protect.
   * @param duration the duration.
   */
  public static void addPlayerProtection(@NotNull OfflinePlayer player, @NotNull Duration duration) {
    instance.addPlayerProtection(player, duration);
  }

  /**
   * Cancel a protection.
   * @param player the player to cancel the protection of.
   */
  public static void removePlayerProtection(@NotNull OfflinePlayer player) {
    instance.removePlayerProtection(player);
  }

}
