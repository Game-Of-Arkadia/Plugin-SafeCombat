package fr.gameofarkadia.safecombat.wanted;

import fr.gameofarkadia.purses.PurseAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import lombok.NoArgsConstructor;
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
//    HuskSyncHelper.clearInventory(Bukkit.getOfflinePlayer(uuid)).whenComplete((result, err) -> {
//      if(err != null) {
//        Main.logger().error("Failed to clear inventory of player {}.", uuid, err);
//        return;
//      }
//      SafeCombatScheduler.run(() -> {
//        Main.logger().info("Inventory of player {} cleared. Dropping content on groud ({}).", uuid, location);
//        result.forEach(itemStack -> location.getWorld().dropItem(location, itemStack));
//      });
//    });

    //XXX Ok, alors HuskSync déconne, et les données ne sont pas vraiment appliquées... donc on va juste drop TOUTE la bourse du
    // type qui déco combat.
    var purse = PurseAPI.getPurse(uuid);
    double balance = purse.getBalance();
    purse.setBalance(0);
    SafeCombatScheduler.run(() -> PurseAPI.getMoneyItemManager().spawnMoney(location, balance));
  }

}
