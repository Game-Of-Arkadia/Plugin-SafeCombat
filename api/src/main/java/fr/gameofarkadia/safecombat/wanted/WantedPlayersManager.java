package fr.gameofarkadia.safecombat.wanted;

import org.bukkit.OfflinePlayer;
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

  @NotNull WantedPlayer getWanted(@NotNull OfflinePlayer player) throws NoSuchElementException;

  void setLocalWanted(@NotNull WantedPlayer player);

  void setNetworkWanted(@NotNull WantedPlayer data);

  default void clearLocalWanted(@NotNull OfflinePlayer player) {
    clearLocalWanted(player.getUniqueId());
  }
  void clearLocalWanted(@NotNull UUID uuid);

  default void clearNetworkWanted(@NotNull OfflinePlayer player) {
    clearNetworkWanted(player.getUniqueId());
  }

  void clearNetworkWanted(@NotNull UUID uuid);

}
