package fr.gameofarkadia.safecombat;

import fr.gameofarkadia.arkadialib.api.ArkadiaLib;
import fr.gameofarkadia.safecombat.bridge.WGBridge;
import fr.gameofarkadia.safecombat.command.ProtectionCommand;
import fr.gameofarkadia.safecombat.configuration.GeneralConfiguration;
import fr.gameofarkadia.safecombat.listener.ForceFieldListener;
import fr.gameofarkadia.safecombat.listener.SafeCombatListener;
import fr.gameofarkadia.safecombat.storage.PlayerStatesManager;
import fr.gameofarkadia.safecombat.sync.BungeeSynchronizer;
import fr.gameofarkadia.safecombat.util.Lang;
import fr.gameofarkadia.safecombat.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

@Slf4j
public final class Main extends JavaPlugin {

    private static Main INSTANCE;
    @Getter private static Lang lang;
    @Getter private static boolean isWGEnabled;

    @Getter private static final Set<UUID> kickedPlayers = new HashSet<>();
    @Getter private static final Set<UUID> diedPlayers = new HashSet<>();

    private GeneralConfiguration configuration;
    private BungeeSynchronizer synchronizer;
    private PlayerStatesManager manager;

    @Override
    public void onLoad() {
        SafeCombatScheduler.setPlugin(this);
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            isWGEnabled = true;
            WGBridge.load();
        }
        configuration = new GeneralConfiguration(getDataFolder());
        configuration.reload();
        synchronizer = new BungeeSynchronizer(this);
        SafeCombatAPI.initialize(combatManager);
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
        lang = new Lang(configuration.getLanguage());

        if(isWGEnabled && !configuration.getPvpConfiguration().isEnderpearlBypassForceField()) {
            Bukkit.getPluginManager().registerEvents(new ForceFieldListener(), this);
            Bukkit.getConsoleSender().sendMessage(Util.prefix() + lang.get("dependency.worldguard"));
        }

        ArkadiaLib.getDatabaseManager().getMigrationsManager().newProject(this, configuration.getDatabaseName())
            .registerAllInJar("migrations")
            .applyMigrations()
            .whenComplete((ver, err) -> {
               if(err != null) {
                   logger().error("Could not migrate database.", err);
               } else {
                 logger().info("Database migrated to version {}.", ver);
               }
            });

        // Setup command, listeners and managers
        new ProtectionCommand();
        Bukkit.getPluginManager().registerEvents(new SafeCombatListener(), this);
    }

    @Override
    public void onDisable() {
        // database close
    }

    @Deprecated
    public static Main getInstance() {
        return INSTANCE;
    }

    /**
     * Get the configuration entry-point.
     * @return a non-null config instance. Will never change.
     */
    public static @NotNull GeneralConfiguration config() {
        return INSTANCE.configuration;
    }

    /**
     * Get plugin logger instance.
     * @return the SLF4J logger.
     */
    public static @NotNull Logger logger() {
        return INSTANCE.getSLF4JLogger();
    }

    /**
     * Get the plugin synchronizer.
     * @return the synchronizer singleton.
     */
    public static @NotNull BungeeSynchronizer synchronizer() {
        return INSTANCE.synchronizer;
    }

    public static @NotNull PlayerStatesManager getCombatManager() {
        return INSTANCE.manager;
    }

}