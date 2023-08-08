package fr.keykatyu.safecombat;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import fr.keykatyu.safecombat.listener.EnterSafeZoneFlagHandler;
import fr.keykatyu.safecombat.listener.SafeCombatListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main INSTANCE;
    private static CombatManager combatManager;
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

        combatManager = new CombatManager();
        getServer().getPluginManager().registerEvents(new SafeCombatListener(), this);
        // Register WorldGuard flag handler
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(EnterSafeZoneFlagHandler.FACTORY, null);
    }

    public static Main getInstance() {
        return INSTANCE;
    }

    public static CombatManager getCombatManager() {
        return combatManager;
    }
}