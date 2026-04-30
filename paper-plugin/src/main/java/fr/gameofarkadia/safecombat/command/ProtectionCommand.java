package fr.gameofarkadia.safecombat.command;

import fr.gameofarkadia.arkadialib.api.commands.PlayerArkadiaCommand;
import fr.gameofarkadia.arkadialib.api.gui.inventory.ConfirmationGUI;
import fr.gameofarkadia.arkadialib.api.utils.DurationUtils;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

/**
 * Player command to disable their protection.
 */
public class ProtectionCommand extends PlayerArkadiaCommand {

  private static final List<String> ARGS = List.of("disable", "status");

  /**
   * Create and register command.
   */
  public ProtectionCommand() {
    super("protection");
  }

  @Override
  protected boolean handleCommand(@NotNull Player player, @NotNull String @NotNull [] args) {
    if (args.length == 0) {
      player.sendMessage(Main.prefix() + "Il manque un argument : " + ARGS + ".");
      return true;
    }

    if("disable".equalsIgnoreCase(args[0])) {
      if(SafeCombatAPI.isProtected(player)) {
        new ConfirmationGUI(
            () -> {
              player.sendMessage(Main.prefix() + "§4Protection désactivée.");
              Main.logger().info("Player {} disabled their protection.", player.getName());
              SafeCombatAPI.getProtectionManager().removePlayerProtection(player);
              // Sound around !
              player.getWorld().playSound(player, Sound.BLOCK_GLASS_BREAK, 1f, 0.8f);
            },
            () -> player.sendMessage(Main.prefix() + "Désactivation de la protection annulée.")
        ).show(player);
      } else {
        player.sendMessage(Main.prefix() + "§cVous n'êtes pas protégé.");
      }
      return true;
    }

    if("status".equalsIgnoreCase(args[0])) {
      if(SafeCombatAPI.isProtected(player)) {
        Duration remaining = SafeCombatAPI.getProtectionManager().getRemainingDuration(player);
        player.sendMessage(Main.prefix() + "§aVous êtes protégé pendant encore §e" + DurationUtils.formatDuration(remaining) + "§a.");
      } else {
        player.sendMessage(Main.prefix() + "§7Vous n'êtes pas protégé.");
      }
      return true;
    }

    player.sendMessage(Main.prefix() + "Argument inconnu : " + args[0] + ". Arguments valides : " + ARGS + ".");
    return true;
  }


  @Override
  protected @Nullable List<String> handleTabComplete(@NotNull Player sender, @NotNull String @NotNull [] args) {
    if(args.length == 1) {
      return ARGS.stream().filter(arg -> StringUtil.startsWithIgnoreCase(arg, args[0]))
          .toList();
    }
    return List.of();
  }

}
