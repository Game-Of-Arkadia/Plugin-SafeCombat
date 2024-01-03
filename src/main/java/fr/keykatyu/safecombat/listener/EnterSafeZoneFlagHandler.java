package fr.keykatyu.safecombat.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.entity.Player;

import java.util.Set;

public class EnterSafeZoneFlagHandler extends FlagValueChangeHandler<StateFlag.State> {
    public static final Factory FACTORY = new Factory();
    public static StateFlag ENTER_SAFE_ZONE_PVP;

    public static class Factory extends Handler.Factory<EnterSafeZoneFlagHandler> {
        @Override
        public EnterSafeZoneFlagHandler create(Session session) {
            return new EnterSafeZoneFlagHandler(session);
        }
    }

    protected EnterSafeZoneFlagHandler(Session session) {
        super(session, ENTER_SAFE_ZONE_PVP);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer localPlayer, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        boolean allowedInPvP = toSet.testState(localPlayer, ENTER_SAFE_ZONE_PVP);
        Player player = BukkitAdapter.adapt(localPlayer);
        if(Main.getCombatManager().isFighting(player) && !allowedInPvP) {
            player.sendMessage(Util.prefix() + Main.getLang().get("fight.cant-enter-zone"));
            player.setHealth(player.getHealth() - 1.0);
            return false;
        }
        return true;
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, StateFlag.State value) {}

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State currentValue, StateFlag.State lastValue, MoveType moveType) {
        return false;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State lastValue, MoveType moveType) {
        return false;
    }

}
