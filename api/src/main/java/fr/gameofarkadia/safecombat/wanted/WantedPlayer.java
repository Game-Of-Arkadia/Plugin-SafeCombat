package fr.gameofarkadia.safecombat.wanted;

import fr.gameofarkadia.safecombat.SafeCombatAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * A wanted player across network.
 * @param uuid UUID of the player.
 * @param serverId server ID wanted at.
 * @param timestamp wanted emission date.
 */
public record WantedPlayer(
    @NotNull UUID uuid,
    @NotNull String serverId,
    long timestamp
) {

  /**
   * Check if this wanted-DTO is form this current server.
   * @return true if this emitted the wanted.
   */
  public boolean isLocal() {
    return Objects.equals(serverId, SafeCombatAPI.getServerId());
  }

  /**
   * Create a new instance.
   * @param player Player.
   * @return a new instance of wanted data.
   */
  @Contract("_ -> new")
  public static @NotNull WantedPlayer of(@NotNull OfflinePlayer player) {
    return of(player.getUniqueId());
  }

  /**
   * Create a new instance.
   * @param uuid Player UUID.
   * @return a new instance of wanted data.
   */
  @Contract("_ -> new")
  public static @NotNull WantedPlayer of(@NotNull UUID uuid) {
    return new WantedPlayer(uuid, SafeCombatAPI.getServerId(), System.currentTimeMillis());
  }

}
