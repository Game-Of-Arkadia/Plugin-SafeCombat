package fr.gameofarkadia.safecombat.bridge;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import fr.gameofarkadia.safecombat.Main;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class WGBridge {

  @Getter private static StateFlag bypassSafeZone;
  @Getter private static StateFlag protectionOnJoin;

  // Safe-zone = PVP disabled

  /**
   * Load the flat to WG.
   */
  public static void initialize() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    try {
      StateFlag enterSafeZonePvPFlag = new StateFlag("bypass-safezone", false);
      registry.register(enterSafeZonePvPFlag);
      bypassSafeZone = enterSafeZonePvPFlag;
    } catch (FlagConflictException e) {
      Flag<?> existing = registry.get("enter-safe-zone-pvp");
      if (existing instanceof StateFlag) {
        bypassSafeZone = (StateFlag) existing;
      }
    }
    try {
      StateFlag enterSafeZonePvPFlag = new StateFlag("protection-on-join", true);
      registry.register(enterSafeZonePvPFlag);
      protectionOnJoin = enterSafeZonePvPFlag;
    } catch (FlagConflictException e) {
      Main.logger().warn("Failed to register WG flag 'protection-on-join'. It may already exist.");
    }
  }

}