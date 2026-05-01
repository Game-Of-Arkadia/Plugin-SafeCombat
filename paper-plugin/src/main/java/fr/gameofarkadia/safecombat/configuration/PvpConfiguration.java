package fr.gameofarkadia.safecombat.configuration;

import fr.gameofarkadia.arkadialib.api.utils.DurationWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Player-versus-player configuration.
 */
@Getter @NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PvpConfiguration {

  private DurationWrapper fightDuration;
  private DurationWrapper durationBeforePunishment;
  private DurationWrapper newbieProtectionDuration;
  private DurationWrapper serverJoinProtectionDuration;
  private DurationWrapper respawnDuration;

  void reload(@NotNull ConfigurationSection root) {
    // Durations
    fightDuration = DurationWrapper.parse(root.getString("durations.fight", "5m"));
    durationBeforePunishment = DurationWrapper.parse(root.getString("durations.allowed-disconnection", "10s"));
    newbieProtectionDuration = DurationWrapper.parse(root.getString("durations.newbie-protection", "2d"));
    serverJoinProtectionDuration = DurationWrapper.parse(root.getString("durations.server-join-protection", "5s"));
    respawnDuration = DurationWrapper.parse(root.getString("durations.respawn-protection", "20s"));
  }

  public boolean hasRespawnProtection() {
    return respawnDuration.asTicks() > 0;
  }

  public boolean hasDisconnectPunishment() {
    return durationBeforePunishment.asTicks() > 0;
  }

}
