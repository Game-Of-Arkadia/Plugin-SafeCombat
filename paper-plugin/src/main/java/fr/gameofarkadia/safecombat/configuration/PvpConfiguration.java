package fr.gameofarkadia.safecombat.configuration;

import fr.gameofarkadia.arkadialib.api.utils.DurationWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
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

  private DurationWrapper riptideCooldown;
  private boolean riptideEnabled = false;

  private DurationWrapper enderpearlCooldown;
  private boolean enderpearlCooldownEnabled = false;
  private boolean enderpearlBypassForceField = false;

  private boolean forceFieldEnabled = false;
  private int forceFieldWidth;
  private int forceFieldHeight;
  private Material forceFieldMaterial;

  void reload(@NotNull ConfigurationSection root) {
    // Durations
    fightDuration = DurationWrapper.parse(root.getString("durations.fight", "5m"));
    durationBeforePunishment = DurationWrapper.parse(root.getString("durations.allowed-disconnection", "10s"));
    newbieProtectionDuration = DurationWrapper.parse(root.getString("durations.newbie-protection", "2d"));
    serverJoinProtectionDuration = DurationWrapper.parse(root.getString("durations.server-join-protection", "5s"));
    respawnDuration = DurationWrapper.parse(root.getString("durations.respawn-protection", "20s"));

    // Trident
    riptideEnabled = root.getBoolean("trident-cooldown.enabled", true);
    riptideCooldown = DurationWrapper.parse(root.getString("trident-cooldown.duration", "15s"));

    //Ender-pearl
    enderpearlCooldownEnabled = root.getBoolean("enderpearl.custom-cooldown", true);
    enderpearlCooldown = DurationWrapper.parse(root.getString("enderpearl.cooldown-time", "10s"));
    enderpearlBypassForceField = root.getBoolean("enderpearl.bypass-force-field", true);

    // Force-field
    forceFieldEnabled = root.getBoolean("force-field.enabled", true);
    forceFieldWidth = root.getInt("force-field.radius", 3);
    forceFieldHeight = root.getInt("force-field.height", 3);
    String rawMaterial =  root.getString("force-field.material", "GLASS");
    try {
      forceFieldMaterial = Material.valueOf(rawMaterial.toUpperCase());
    } catch(IllegalArgumentException e) {
      forceFieldMaterial = Material.GLASS;
    }
  }

  public boolean hasRespawnProtection() {
    return respawnDuration.asTicks() > 0;
  }

  public boolean hasDisconnectPunishment() {
    return durationBeforePunishment.asTicks() > 0;
  }

}
