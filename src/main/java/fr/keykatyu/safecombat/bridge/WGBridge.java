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

package fr.keykatyu.safecombat.bridge;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;

public class WGBridge {

    public static StateFlag ENTER_SAFE_ZONE_PVP;

    public static void load() {
        // Register custom WG flag
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

    /**
     * Get if the region is a safezone with ENTER_SAFE_ZONE_PVP flag value
     * @param player The player
     * @param location The location
     * @return True or false
     */
    public static boolean isSafeZoneAt(Player player, Location location) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.queryValue(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), ENTER_SAFE_ZONE_PVP) == StateFlag.State.DENY;
    }

    /**
     * Retrieve WorldGuard region edges blocks
     * @param region The region
     * @param world The region's world
     * @param y The edge y
     * @return List of locations, corresponding to blocks
     */
    public static List<Location> retrieveEdgesBlocks(ProtectedRegion region, World world, double y) {
        List<Location> edgesBlocks = new ArrayList<>();
        List<BlockVector2> points = region.getPoints();

        BlockVector2 previousPoint = points.get(0);
        for(int i = 1; i < points.size(); i++) {
            BlockVector2 point = points.get(i);
            Location pointLocation = new Location(world, point.getBlockX(), y, point.getBlockZ());
            Location previousLocation = new Location(world, previousPoint.getBlockX(), y, previousPoint.getBlockZ());
            org.bukkit.util.Vector previousPointVector = previousLocation.toVector();
            org.bukkit.util.Vector pointVector = pointLocation.toVector();
            org.bukkit.util.Vector directionVector = pointVector.subtract(previousPointVector);

            BlockIterator blocksBetweenPoints = new BlockIterator(world, previousPointVector, directionVector,
                    0, ((Double) pointLocation.distance(previousLocation)).intValue());
            while(blocksBetweenPoints.hasNext()){
                Block block = blocksBetweenPoints.next();
                edgesBlocks.add(block.getLocation());
            }

            previousPoint = point;
        }
        return edgesBlocks;
    }

}