package fr.gameofarkadia.safecombat.protection;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data about a player to be killed.
 * @param player player to kill.
 * @param protectionStarted date-time of creation of this protect-order.
 * @param protectionFinish date-time of termination of this order.
 */
public record ProtectedData(
    @NotNull UUID player,
    @NotNull LocalDateTime protectionStarted,
    @NotNull LocalDateTime protectionFinish
) {

  /**
   * Generate a new protected-data.
   * @param player player to protect.
   * @param duration duration of the protection.
   * @return a new instance.
   */
  @Contract("_,_ -> new")
  public static @NotNull ProtectedData generate(@NotNull OfflinePlayer player, @NotNull Duration duration) {
    LocalDateTime start = LocalDateTime.now();
    return new ProtectedData(player.getUniqueId(), start, start.plus(duration));
  }

  /**
   * Duration of this order.
   * @return a non-null duration.
   */
  public @NotNull Duration duration() {
    return Duration.between(protectionStarted, protectionFinish);
  }

  /**
   * Duration of this order.
   * @return a non-null duration.
   */
  public @NotNull Duration remaining() {
    return Duration.between(LocalDateTime.now(), protectionFinish);
  }

  /**
   * Check if this protection is over.
   * @return true if "finish" date-time is after "now".
   */
  public boolean isOver() {
    return LocalDateTime.now().isAfter(protectionFinish);
  }
}
