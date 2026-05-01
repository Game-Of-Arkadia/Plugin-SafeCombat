package fr.gameofarkadia.safecombat.wanted;

import fr.gameofarkadia.purses.PurseAPI;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.sync.SyncCommand;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Static helper for punishments.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PunishmentHelper {

  /**
   * Apply a punishment.
   * @param uuid UUID of the player to punish.
   * @param location location to drop items.
   */
  public static void applyPunishment(@NotNull UUID uuid, @NotNull Location location) {
    if(Main.config().getPunishmentConfiguration().isLoseMoneyEnabled()) {
      var purse = PurseAPI.getPurse(uuid);
      double balance = purse.getBalance();
      purse.setBalance(0);
      SafeCombatScheduler.run(() -> PurseAPI.getMoneyItemManager().spawnMoney(location, balance));
    }

    if(Main.config().getPunishmentConfiguration().isBanEnabled()) {
      banPlayer(uuid);
    }
  }

  private static void banPlayer(@NotNull UUID uuid) {
    var config = Main.config().getPunishmentConfiguration();

    if(SafeCombatAPI.getServerId().equalsIgnoreCase(config.getBanMainServer())) {
      Main.logger().info("We are the main server. Banning {}.", uuid);
      Bukkit.getOfflinePlayer(uuid).ban(
          config.getBanReason(),
          config.getBanDuration().asJavaDuration(),
          "SafeCombat"
      );
      return;
    }

    // Push to remote if not main server.
    Main.synchronizer().sendRpc(SyncCommand.BAN_PLAYER, uuid, SafeCombatAPI.getServerId());
  }

  public static void notifyBanFromServer(@NotNull UUID uuid, @NotNull String serverFrom) {
    var config = Main.config().getPunishmentConfiguration();

    Main.logger().info("Received ban request from {}. Banning {}.", serverFrom, uuid);
    Bukkit.getOfflinePlayer(uuid).ban(
        config.getBanReason(),
        config.getBanDuration().asJavaDuration(),
        "SafeCombat"
    );
  }

}
