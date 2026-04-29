package fr.gameofarkadia.safecombat;

import com.google.common.base.Preconditions;
import fr.gameofarkadia.safecombat.combat.CombatManager;
import fr.gameofarkadia.safecombat.protection.ProtectionManager;
import fr.gameofarkadia.safecombat.wanted.WantedPlayersManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Plugin entry-point.
 */
public final class SafeCombatAPI {
  private SafeCombatAPI() {}

  private static SafeCombatPlugin instance;

  @ApiStatus.Internal
  static void initialize(@NotNull SafeCombatPlugin plugin) {
    Preconditions.checkState(instance == null, "SafeCombatAPI is already initialized");
    instance = plugin;
  }

  public static @NotNull String getServerId() {
    return instance.getServerId();
  }

  /**
   * Check if player is fighting.
   * @param playerUUID The player UUID.
   * @return {@code true} if the player is in a fight.
   */
  public static boolean isFighting(@NotNull UUID playerUUID) {
    return instance.getCombatManager().isFighting(playerUUID);
  }

  /**
   * Check if player is fighting.
   * @param player The player to check.
   * @return {@code true} if the player is in a fight.
   */
  public static boolean isFighting(@NotNull OfflinePlayer player) {
    return instance.getCombatManager().isFighting(player);
  }

  /**
   * Check if player is protected.
   * @param playerUUID The player UUID.
   * @return {@code true} if the player cannot be attacked.
   */
  public static boolean isProtected(@NotNull UUID playerUUID) {
    return instance.getProtectionManager().isProtected(playerUUID);
  }

  /**
   * Check if player is protected.
   * @param player The player to check.
   * @return {@code true} if the player cannot be attacked.
   */
  public static boolean isProtected(@NotNull OfflinePlayer player) {
    return instance.getProtectionManager().isProtected(player);
  }

  public static boolean isWanted(@NotNull OfflinePlayer player) {
    return instance.getWantedPlayersManager().isWanted(player);
  }

  public static boolean isWanted(@NotNull UUID playerUUID) {
    return instance.getWantedPlayersManager().isWanted(playerUUID);
  }

  public static @NotNull CombatManager getCombatManager() {
    return instance.getCombatManager();
  }

  public static @NotNull ProtectionManager getProtectionManager() {
    return instance.getProtectionManager();
  }

  public static @NotNull WantedPlayersManager getWantedPlayersManager() {
    return instance.getWantedPlayersManager();
  }



}
