package fr.gameofarkadia.safecombat.storage;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data about a player to be killed.
 * @param player player to kill.
 * @param disconnectServer server-ID of disconnection.
 * @param disconnectTime date-time of disconnection.
 * @param toKillTime date-time of creation of this kill-order.
 */
public record ToKillData(
    @NotNull UUID player,
    @NotNull String disconnectServer,
    @NotNull LocalDateTime disconnectTime,
    @NotNull LocalDateTime toKillTime
) {

  /**
   * Generate a new kill-order.
   * @param player player to kill.
   * @param serverId local server instance ID.
   * @param disconnect original disconnection time.
   * @return a new instance.
   */
  @Contract("_,_,_ -> new")
  public static @NotNull ToKillData generate(@NotNull OfflinePlayer player, @NotNull String serverId, @NotNull LocalDateTime disconnect) {
    return new ToKillData(player.getUniqueId(), serverId, disconnect, LocalDateTime.now());
  }

}
