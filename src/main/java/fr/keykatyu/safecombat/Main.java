package fr.keykatyu.safecombat;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import fr.keykatyu.safecombat.command.ProtectionCommand;
import fr.keykatyu.safecombat.listener.EnterSafeZoneFlagHandler;
import fr.keykatyu.safecombat.listener.SafeCombatListener;
import fr.keykatyu.safecombat.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Main extends JavaPlugin {

    private static Main INSTANCE;
    private static CombatManager combatManager;
    private static final List<String> kickedPlayers = new ArrayList<>();
    private static final List<String> diedPlayers = new ArrayList<>();
    public static StateFlag ENTER_SAFE_ZONE_PVP;

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag enterSafeZonePvPFlag = new StateFlag("enter-safe-zone-pvp", true);
            registry.register(enterSafeZonePvPFlag);
            ENTER_SAFE_ZONE_PVP = enterSafeZonePvPFlag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("enter-safe-zone-pvp");
            if (existing instanceof StateFlag) {
                ENTER_SAFE_ZONE_PVP = (StateFlag) existing;
            }
        }
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        getCommand("protection").setExecutor(new ProtectionCommand());
        combatManager = new CombatManager(Config.getStringList("playerstokill"), Config.getMap("protected-players"));
        Bukkit.getPluginManager().registerEvents(new SafeCombatListener(), this);

        // Register WorldGuard flag handler
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(EnterSafeZoneFlagHandler.FACTORY, null);
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

    public static List<String> getKickedPlayers() {
        return kickedPlayers;
    }

    public static List<String> getDiedPlayers() {
        return diedPlayers;
    }

}