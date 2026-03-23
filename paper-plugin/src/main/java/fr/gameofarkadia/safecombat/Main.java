package fr.gameofarkadia.safecombat;

import fr.gameofarkadia.safecombat.bridge.WGBridge;
import fr.gameofarkadia.safecombat.command.ProtectionCommand;
import fr.gameofarkadia.safecombat.listener.ForceFieldListener;
import fr.gameofarkadia.safecombat.listener.SafeCombatListener;
import fr.gameofarkadia.safecombat.util.Config;
import fr.gameofarkadia.safecombat.util.Lang;
import fr.gameofarkadia.safecombat.util.LocalStorage;
import fr.gameofarkadia.safecombat.util.Util;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin {

    private static Main INSTANCE;
    @Getter private static final CombatManager combatManager = new CombatManager();
    @Getter  private static Lang lang;
    @Getter private static boolean isWGEnabled;

    @Getter private static final Set<UUID> kickedPlayers = new HashSet<>();
    @Getter private static final Set<UUID> diedPlayers = new HashSet<>();

    private LocalStorage localStorage;

    @Override
    public void onLoad() {
        SafeCombatAPI.initialize(combatManager);
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            isWGEnabled = true;
            WGBridge.load();
        }
        localStorage = new LocalStorage(getDataFolder());
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
        lang = new Lang(getConfig().getString("language", "fr"));

        if(isWGEnabled && !Config.getBoolean("pvp.enderpearl.back-safezone")) {
            Bukkit.getPluginManager().registerEvents(new ForceFieldListener(), this);
            Bukkit.getConsoleSender().sendMessage(Util.prefix() + lang.get("dependency.worldguard"));
        }

        // Setup command, listeners and managers
        new ProtectionCommand();
        Bukkit.getPluginManager().registerEvents(new SafeCombatListener(), this);

        localStorage.reload();
    }

    @Override
    public void onDisable() {
        localStorage.save();
    }

    public static Main getInstance() {
        return INSTANCE;
    }

}