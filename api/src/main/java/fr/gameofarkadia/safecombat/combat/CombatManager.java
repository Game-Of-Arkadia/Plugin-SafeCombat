package fr.gameofarkadia.safecombat.combat;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * Interface to manage fighting states of players.
 */
public interface CombatManager {

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

  void setStartedFight(@NotNull Player player, boolean isAttacked);

  default void clearFightStatus(@NotNull OfflinePlayer player) {
    clearFightStatus(player.getUniqueId());
  }

  void clearFightStatus(@NotNull UUID player);

}
