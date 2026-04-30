package fr.gameofarkadia.safecombat.command;

import fr.gameofarkadia.arkadialib.api.commands.PlayerArkadiaCommand;
import fr.gameofarkadia.arkadialib.api.utils.DurationUtils;
import fr.gameofarkadia.arkadialib.api.utils.DurationWrapper;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.protection.ProtectionReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

/**
 * Admin command to manage plugin.
 */
public class AdminCommand extends PlayerArkadiaCommand {

  private static final List<String> ARGS = List.of("protection.disable", "protection.set");

  /**
   * Create and register command.
   */
  public AdminCommand() {
    super("safecombat-admin");
  }

  @Override
  protected boolean handleCommand(@NotNull Player player, @NotNull String @NotNull [] args) {
    if (args.length == 0) {
      player.sendMessage(Main.prefix() + "§cUnknown arg. Expected one of " + ARGS);
      return true;
    }

    if(args.length == 1) {
      player.sendMessage(Main.prefix() + "§cSpecify the player name.");
      return true;
    }
    Player target = Bukkit.getPlayer(args[0]);
    if(target == null) {
      player.sendMessage(Main.prefix() + "§cPlayer not found.");
      return true;
    }

    // Disable protection
    if("protection.disable".equals(args[0])) {
      if (!SafeCombatAPI.isProtected(player)) {
        player.sendMessage(Main.prefix() + "§cCe joueur n'est pas protégé.");
        return true;
      }
      SafeCombatAPI.getProtectionManager().removePlayerProtection(target);
      target.sendMessage(Main.prefix() + "§4Un administrateur a retiré votre protection.");
      return true;
    }

    // Add protection
    if("protection.set".equals(args[0])) {
      if(args.length == 2) {
        player.sendMessage(Main.prefix() + "§cSpecify the duration. Format: \"3h\" or \"15m\" for example.");
        return true;
      }
      Duration duration;
      try {
        duration = DurationWrapper.parse(args[2]).asJavaDuration();
      } catch (IllegalArgumentException err) {
        player.sendMessage(Main.prefix() + "§cInvalid duration format \"§4"+args[2]+"§c\" the duration. Format: \"3h\" or \"15m\" for example.");
        return true;
      }

      SafeCombatAPI.getProtectionManager().addPlayerProtection(target, ProtectionReason.ADMINISTRATOR, duration);
      target.sendMessage(Main.prefix() + "§2Un administrateur vous a ajouté une protection de§6 "+DurationUtils.formatDuration(duration)+"§2.");
      player.sendMessage(Main.prefix() + "§2Protection added to §6" + target.getName() + "§2 for §6" + DurationUtils.formatDuration(duration) + "§2.");
      return true;
    }

    player.sendMessage(Main.prefix() + "§cUnknown arg. Expected one of " + ARGS);
    return true;
  }


  @Override
  protected @Nullable List<String> handleTabComplete(@NotNull Player sender, @NotNull String @NotNull [] args) {
    if(args.length == 1) {
      return ARGS.stream().filter(arg -> StringUtil.startsWithIgnoreCase(arg, args[0]))
          .toList();
    }
    if(args.length == 2) {
      return Bukkit.getOnlinePlayers().stream().map(Player::getName)
          .filter(name -> StringUtil.startsWithIgnoreCase(name, args[1]))
          .toList();
    }
    return List.of();
  }

}
