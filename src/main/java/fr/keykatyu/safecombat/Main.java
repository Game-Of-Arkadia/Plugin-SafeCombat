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

package fr.keykatyu.safecombat;

import fr.keykatyu.safecombat.bridge.WGBridge;
import fr.keykatyu.safecombat.command.ProtectionCommand;
import fr.keykatyu.safecombat.listener.ForceFieldListener;
import fr.keykatyu.safecombat.listener.SafeCombatListener;
import fr.keykatyu.safecombat.util.Config;
import fr.keykatyu.safecombat.util.Lang;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Main extends JavaPlugin {

    private static Main INSTANCE;
    private static CombatManager combatManager;
    private static Lang lang;
    private static boolean isWGEnabled;

    private static final List<String> kickedPlayers = new ArrayList<>();
    private static final List<String> diedPlayers = new ArrayList<>();

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            isWGEnabled = true;
            WGBridge.load();
        }
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Setup lang files
        Lang.setupFiles();
        lang = new Lang(getConfig().getString("language"));

        if(isWGEnabled) {
            Bukkit.getPluginManager().registerEvents(new ForceFieldListener(), this);
            getServer().getConsoleSender().sendMessage(Util.prefix() + lang.get("dependency.worldguard"));
        }

        // Setup command, listeners and managers
        getCommand("protection").setExecutor(new ProtectionCommand());
        combatManager = new CombatManager(Config.getStringList("playerstokill"), Config.getMap("protected-players"));
        Bukkit.getPluginManager().registerEvents(new SafeCombatListener(), this);
    }

    @Override
    public void onDisable() {
        Config.setStringList("playerstokill", combatManager.getPlayersToKill());
        Map<String, Object> protectedPlayers = new HashMap<>();
        combatManager.getProtectedPlayers().forEach((uuid, task) -> protectedPlayers.put(uuid.toString(), task.getProtectionEnd().toEpochMilli()));
        Config.setMap("protected-players", protectedPlayers);
    }

    public static Main getInstance() {
        return INSTANCE;
    }

    public static CombatManager getCombatManager() {
        return combatManager;
    }

    public static Lang getLang() {
        return lang;
    }

    public static boolean isWGEnabled() {
        return isWGEnabled;
    }

    public static List<String> getKickedPlayers() {
        return kickedPlayers;
    }

    public static List<String> getDiedPlayers() {
        return diedPlayers;
    }

}