package fr.gameofarkadia.safecombat.wanted;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.bridge.HuskSyncHelper;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PunishmentHelper {

  /**
   * Apply a punishment.
   * @param uuid UUID of the player to punish.
   * @param location location to drop items.
   */
  public static void applyPunishment(@NotNull UUID uuid, @NotNull Location location) {
    // Clear stuff with HuskSync
    HuskSyncHelper.clearInventory(Bukkit.getOfflinePlayer(uuid)).whenComplete((result, err) -> {
      if(err != null) {
        Main.logger().error("Failed to clear inventory of player {}.", uuid, err);
        return;
      }
      Main.logger().info("Inventory of player {} cleared.", uuid);
      result.forEach(itemStack -> location.getWorld().dropItem(location, itemStack));
    });
  }

}
