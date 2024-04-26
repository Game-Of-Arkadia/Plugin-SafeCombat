/*
 * Copyright (C) 2024. KeyKatyu / Antoine D. (keykatyu@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.keykatyu.safecombat.command;

import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.listener.ConfirmDisableProtection;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProtectionCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Util.prefix() + Main.getLang().get("must-be-player"));
            return false;
        }

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
                    Main.getCombatManager().setPlayerProtected(target, Instant.now().plus(time, unit), 1200);
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
                Main.getCombatManager().getProtectedPlayers().get(target.getUniqueId()).cancel();
                Main.getCombatManager().getProtectedPlayers().remove(target.getUniqueId());
                target.sendMessage(Util.prefix() + Main.getLang().get("protection.removed-admin"));
                player.sendMessage(Util.prefix() + Main.getLang().get("protection.target-removed"));
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
