package fr.gameofarkadia.safecombat;

import fr.gameofarkadia.arkadialib.api.ArkadiaLib;
import fr.gameofarkadia.safecombat.bridge.HuskSyncHelper;
import fr.gameofarkadia.safecombat.combat.CombatManager;
import fr.gameofarkadia.safecombat.combat.CombatManagerImpl;
import fr.gameofarkadia.safecombat.command.ProtectionCommand;
import fr.gameofarkadia.safecombat.configuration.GeneralConfiguration;
import fr.gameofarkadia.safecombat.listener.ForceFieldListener;
import fr.gameofarkadia.safecombat.listener.SafeCombatListener;
import fr.gameofarkadia.safecombat.protection.ProtectionManager;
import fr.gameofarkadia.safecombat.protection.ProtectionManagerImpl;
import fr.gameofarkadia.safecombat.sync.IpcSynchronizer;
import fr.gameofarkadia.safecombat.util.Lang;
import fr.gameofarkadia.safecombat.util.Util;
import fr.gameofarkadia.safecombat.wanted.WantedPlayersManager;
import fr.gameofarkadia.safecombat.wanted.WantedPlayersManagerImpl;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

@Getter
public final class Main extends JavaPlugin implements SafeCombatPlugin {

    private static Main INSTANCE;
    @Getter private static Lang lang;

    private GeneralConfiguration configuration;
    private IpcSynchronizer synchronizer;

    private ProtectionManager protectionManager;
    private WantedPlayersManager wantedPlayersManager;
    private CombatManager combatManager;

    @Override
    public void onLoad() {
        SafeCombatScheduler.setPlugin(this);

        configuration = new GeneralConfiguration(getDataFolder());
        configuration.reload();

        combatManager = new CombatManagerImpl();
        wantedPlayersManager = new WantedPlayersManagerImpl();

        synchronizer = new IpcSynchronizer();

        SafeCombatAPI.initialize(this);
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

        if(!configuration.getPvpConfiguration().isEnderpearlBypassForceField()) {
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
               SafeCombatScheduler.run(() -> {
                   try {
                       lateInit();
                   } catch (Exception e) {
                       logger().error("Could not finish lateInit.", e);
                   }
               });
            });

    }

    private void lateInit() {
        // protection manager
        protectionManager = new ProtectionManagerImpl(ArkadiaLib.getDatabaseManager());

        // Setup command, listeners and managers
        new ProtectionCommand();
        Bukkit.getPluginManager().registerEvents(new SafeCombatListener(), this);
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
    public static @NotNull IpcSynchronizer synchronizer() {
        return INSTANCE.synchronizer;
    }

    @Override
    public @NotNull String getServerId() {
        return HuskSyncHelper.getServerId();
    }
}