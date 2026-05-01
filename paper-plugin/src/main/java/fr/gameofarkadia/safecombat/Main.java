package fr.gameofarkadia.safecombat;

import fr.gameofarkadia.arkadialib.api.ArkadiaLib;
import fr.gameofarkadia.safecombat.bridge.HuskSyncHelper;
import fr.gameofarkadia.safecombat.combat.CombatManager;
import fr.gameofarkadia.safecombat.combat.CombatManagerImpl;
import fr.gameofarkadia.safecombat.command.AdminCommand;
import fr.gameofarkadia.safecombat.command.ProtectionCommand;
import fr.gameofarkadia.safecombat.configuration.GeneralConfiguration;
import fr.gameofarkadia.safecombat.connection.FirstPlayerConnectionHandler;
import fr.gameofarkadia.safecombat.listener.*;
import fr.gameofarkadia.safecombat.protection.ProtectionManager;
import fr.gameofarkadia.safecombat.protection.ProtectionManagerImpl;
import fr.gameofarkadia.safecombat.sync.IpcSynchronizer;
import fr.gameofarkadia.safecombat.util.PlayerTransfertHandler;
import fr.gameofarkadia.safecombat.wanted.ToClearPlayersList;
import fr.gameofarkadia.safecombat.wanted.WantedPlayersManager;
import fr.gameofarkadia.safecombat.wanted.WantedPlayersManagerImpl;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

@Getter
public final class Main extends JavaPlugin implements SafeCombatPlugin {

  private static Main INSTANCE;

  private GeneralConfiguration configuration;
  private final IpcSynchronizer synchronizer = new IpcSynchronizer();;
  private PlayerTransfertHandler playerTransfertHandler;
  @Getter private static final ToClearPlayersList toClearPlayersList = new ToClearPlayersList();

  private ProtectionManager protectionManager;
  private WantedPlayersManager wantedPlayersManager;
  private CombatManager combatManager;
  private final FirstPlayerConnectionHandler firstPlayerConnectionHandler = new FirstPlayerConnectionHandler();

  @Override
  public void onLoad() {
    INSTANCE = this;
    SafeCombatScheduler.setPlugin(this);

    configuration = new GeneralConfiguration(getDataFolder());
    configuration.reload();

    combatManager = new CombatManagerImpl();
    wantedPlayersManager = new WantedPlayersManagerImpl();

    SafeCombatAPI.initialize(this);
  }

  @Override
  public void onEnable() {
    saveDefaultConfig();
    reloadConfig();
    getConfig().options().copyDefaults(true);
    saveConfig();

    synchronizer.initialize();
    playerTransfertHandler = new PlayerTransfertHandler(this);

    if (!configuration.getPvpConfiguration().isEnderpearlBypassForceField()) {
      Bukkit.getPluginManager().registerEvents(new ForceFieldListener(), this);
    }

    ArkadiaLib.getDatabaseManager().getMigrationsManager().newProject(this, configuration.getDatabaseName())
        .registerAllInJar("migrations")
        .applyMigrations()
        .whenComplete((ver, err) -> {
          if (err != null) {
            logger().error("Could not migrate database. Disabling plugin.", err);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
          }
          logger().info("Database migrated to version {}.", ver);
          SafeCombatScheduler.run(() -> {
            try {
              lateInit();
            } catch (Exception e) {
              logger().error("Could not finish lateInit.", e);
            }
          });
        });

  }

  @Override
  public void onDisable() {
    if (playerTransfertHandler != null)
      playerTransfertHandler.unregister();
  }

  private void lateInit() {
    firstPlayerConnectionHandler.lateInit();
    toClearPlayersList.lateInit();

    // protection manager
    protectionManager = new ProtectionManagerImpl(ArkadiaLib.getDatabaseManager());

    // Setup command, listeners and managers
    new ProtectionCommand();
    new AdminCommand();
    Bukkit.getPluginManager().registerEvents(new DisconnectCombatListener(), this);
    Bukkit.getPluginManager().registerEvents(new FightListener(), this);
    Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
    Bukkit.getPluginManager().registerEvents(new SafeZoneListener(), this);
  }

  /**
   * Get the configuration entry-point.
   *
   * @return a non-null config instance. Will never change.
   */
  public static @NotNull GeneralConfiguration config() {
    return INSTANCE.configuration;
  }

  /**
   * Get plugin logger instance.
   *
   * @return the SLF4J logger.
   */
  public static @NotNull Logger logger() {
    return INSTANCE.getSLF4JLogger();
  }

  /**
   * Get the plugin synchronizer.
   *
   * @return the synchronizer singleton.
   */
  public static @NotNull IpcSynchronizer synchronizer() {
    return INSTANCE.synchronizer;
  }

  /**
   * Get the plugin player transfert handler.
   *
   * @return the synchronizer singleton.
   */
  public static @NotNull PlayerTransfertHandler playerTransfertHandler() {
    return INSTANCE.playerTransfertHandler;
  }

  public static @NotNull String prefix() {
    return config().getPrefix();
  }

  public static @NotNull FirstPlayerConnectionHandler firstPlayerConnectionHandler() {
    return INSTANCE.firstPlayerConnectionHandler;
  }

  @Override
  public @NotNull String getServerId() {
    return HuskSyncHelper.getServerId();
  }

  public static @Nullable InputStream fetchJarRessource(@NotNull String path) throws IOException {
    return INSTANCE.getResource(path);
  }
}