package fr.gameofarkadia.safecombat.command;

import fr.gameofarkadia.arkadialib.api.commands.PlayerArkadiaCommand;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.listener.ConfirmDisableProtection;
import fr.gameofarkadia.safecombat.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main command.<br/>
 * Player can disable their protection.<br/>
 * Admin can add and remove protections.
 */
public class ProtectionCommand extends PlayerArkadiaCommand {

    /**
     * Create and register command.
     */
    public ProtectionCommand() {
        super("protection");
    }

    @Override
    protected boolean handleCommand(@NotNull Player player, @NotNull String @NotNull [] args) {
        if(args.length < 1 || args.length > 4) {
            player.sendMessage(Util.prefix() + Main.getLang().get("not-enough-args"));
            return false;
        }

        switch (args[0]) {
            case "disable" -> {
                if(!Main.getCombatManager().isProtected(player)) {
                    player.sendMessage(Util.prefix() + Main.getLang().get("protection.not-protected"));
                    return false;
                }
                Bukkit.getPluginManager().registerEvents(new ConfirmDisableProtection(player), Main.getInstance());
                player.sendMessage(Util.prefix() + Main.getLang().get("protection.cancellation-started")
                        .replaceAll("%cancel-code%", Main.getLang().get("protection.cancellation-cancel-code"))
                        .replaceAll("%code%", Main.getLang().get("protection.cancellation-code")));
            }
            case "add" -> {
                if(args.length < 4) {
                    player.sendMessage(Util.prefix() + Main.getLang().get("not-enough-args"));
                    return false;
                }
                if(!player.hasPermission("safecombat.protection")) return false;
                try {
                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null) {
                        player.sendMessage(Util.prefix() + Main.getLang().get("player-offline"));
                        return false;
                    }
                    if(Main.getCombatManager().isProtected(target)) {
                        player.sendMessage(Util.prefix() + Main.getLang().get("protection.already"));
                        return false;
                    }

                    int time = Integer.parseInt(args[2]);
                    ChronoUnit unit = ChronoUnit.valueOf(args[3]);
                    Duration duration =  Duration.of(time, unit);
                    Main.getCombatManager().addPlayerProtection(target, duration);
                    player.sendMessage(Util.prefix() + Main.getLang().get("protection.added-admin"));
                    target.sendMessage(Util.prefix() + Main.getLang().get("protection.added"));
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Util.prefix() + Main.getLang().get("args-wrong"));
                    return false;
                }
            }
            case "remove" -> {
                if(args.length != 2) {
                    player.sendMessage(Util.prefix() + Main.getLang().get("not-enough-args"));
                    return false;
                }
                if(!player.hasPermission("safecombat.protection")) return false;
                Player target = Bukkit.getPlayer(args[1]);
                if(target == null) {
                    player.sendMessage(Util.prefix() + Main.getLang().get("player-offline"));
                    return false;
                }
                if(Main.getCombatManager().removePlayerProtection(target)) {
                    target.sendMessage(Util.prefix() + Main.getLang().get("protection.removed-admin"));
                    player.sendMessage(Util.prefix() + Main.getLang().get("protection.target-removed"));
                } else {
                    player.sendMessage(Util.prefix() + "§cThis player was not protected.");
                }
            }
        }

        return true;
    }


    @Override
    protected @Nullable List<String> handleTabComplete(@NotNull Player sender, @NotNull String @NotNull [] args) {
        List<String> results = new ArrayList<>();
        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                completions.add("disable");
                if(sender.hasPermission("safecombat.protection")) {
                    completions.add("add");
                    completions.add("remove");
                }
                StringUtil.copyPartialMatches(args[0], completions, results);
            }
            case 2 -> {
                if(!sender.hasPermission("safecombat.protection")) return results;
                if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                }
                StringUtil.copyPartialMatches(args[1], completions, results);
            }
            case 4 -> {
                if(!sender.hasPermission("safecombat.protection")) return results;
                if(args[0].equalsIgnoreCase("add")) {
                    List<ChronoUnit> supportedUnits = List.of(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS);
                    completions.addAll(supportedUnits.stream().map(Enum::name).toList());
                }
                StringUtil.copyPartialMatches(args[3], completions, results);
            }
        }
        Collections.sort(results);
        return results;
    }
}
