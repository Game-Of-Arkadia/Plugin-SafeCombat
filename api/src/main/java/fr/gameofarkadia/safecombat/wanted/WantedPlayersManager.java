package fr.gameofarkadia.safecombat.wanted;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Interface to manage wanted players.
 */
public interface WantedPlayersManager {

  boolean isWanted(@NotNull UUID player);

  default boolean isWanted(@NotNull OfflinePlayer player) {
    return isWanted(player.getUniqueId());
  }

  boolean isWantedLocally(@NotNull UUID player);

  default boolean isWantedLocally(@NotNull OfflinePlayer player) {
    return isWantedLocally(player.getUniqueId());
  }

  @NotNull WantedPlayer getWanted(@NotNull OfflinePlayer player) throws NoSuchElementException;

  /**
   * A player disconnected while in combat (on this server).
   * @param player player that disconnected.
   */
  void declareWanted(@NotNull Player player);

  /**
   * Another server sent us a wanted-request on a player.
   * @param data data of the wanted player.
   */
  void receivedRemoveWanted(@NotNull WantedPlayer data);

  /**
   * The player reconnected locally, or punishment has been done.
   * @param uuid UUID of the wanted player.
   */
  void clearLocalWanted(@NotNull UUID uuid);

  /**
   * Either the player has been found somewhere, either task should be removed.
   * @param uuid UUID of the wanted player.
   */
  void clearRemoteWanted(@NotNull UUID uuid);

  void reconnected(@NotNull Player player);

}
