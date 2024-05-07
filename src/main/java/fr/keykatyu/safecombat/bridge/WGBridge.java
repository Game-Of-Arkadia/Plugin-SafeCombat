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
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.*;

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
     * Get all safe zones regions into a list of block
     * @param player The player
     * @param blocks The blocks
     * @return A list of ProtectedRegion, corresponding to safe zones
     */
    public static Set<ProtectedRegion> getSafeZonesInBlocks(Player player, List<BlockVector2> blocks) {
        Set<ProtectedRegion> safeZones = new HashSet<>();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));

        for(Map.Entry<String, ProtectedRegion> regionEntry : manager.getRegions().entrySet()) {
            ProtectedRegion region = regionEntry.getValue();
            if(manager.getApplicableRegions(region).testState(WorldGuardPlugin.inst().wrapPlayer(player), WGBridge.ENTER_SAFE_ZONE_PVP)) continue;
            if(region.containsAny(blocks)) safeZones.add(region);
        }

        return safeZones;
    }

    /**
     * Retrieve WorldGuard region edges blocks (2D) depending on a player
     * @param region The region
     * @param playerLocation The player location
     * @param radius The max distance between the player and an edge block
     * @return List of locations, corresponding to the edges blocks
     */
    public static List<Location> retrieveEdgesBlocks(ProtectedRegion region, Location playerLocation, int radius) {
        List<Location> edgesBlocks = new ArrayList<>();

        switch (region.getType()) {
            case CUBOID -> {
                // Get min and max coordinates of the cuboid region
                int minX = region.getMinimumPoint().getBlockX();
                int minZ = region.getMinimumPoint().getBlockZ();
                int maxX = region.getMaximumPoint().getBlockX();
                int maxZ = region.getMaximumPoint().getBlockZ();

                for (int x = minX; x <= maxX; x++) {
                    if (x == minX || x == maxX) {
                        for (int z = minZ; z <= maxZ; z++) {
                            Location blockLocation = new Location(playerLocation.getWorld(), x, playerLocation.getBlockY(), z);
                            if (Math.abs(z - playerLocation.getBlockZ()) <= radius) {
                                edgesBlocks.add(blockLocation);
                            }
                        }
                    } else {
                        Location minZBlock = new Location(playerLocation.getWorld(), x, playerLocation.getBlockY(), minZ);
                        Location maxZBlock = new Location(playerLocation.getWorld(), x, playerLocation.getBlockY(), maxZ);

                        if (minZBlock.distance(playerLocation) <= radius) {
                            edgesBlocks.add(minZBlock);
                        }
                        if (maxZBlock.distance(playerLocation) <= radius) {
                            edgesBlocks.add(maxZBlock);
                        }
                    }
                }
            }
            case POLYGON -> {
                List<BlockVector2> points = region.getPoints();

                // Loop all the points each other from the first to the last
                // to get edges blocks
                BlockVector2 previousPoint = points.get(0);
                for(int i = 1; i <= points.size(); i++) {
                    if(i == points.size()) i = 0;

                    BlockVector2 point = points.get(i);
                    Location pointLocation = new Location(playerLocation.getWorld(), point.getBlockX(), playerLocation.getBlockY(), point.getBlockZ());
                    Location previousLocation = new Location(playerLocation.getWorld(), previousPoint.getBlockX(), playerLocation.getBlockY(), previousPoint.getBlockZ());
                    org.bukkit.util.Vector previousPointVector = previousLocation.toVector();
                    org.bukkit.util.Vector pointVector = pointLocation.toVector();
                    org.bukkit.util.Vector directionVector = pointVector.subtract(previousPointVector);

                    BlockIterator blocksBetweenPoints = new BlockIterator(playerLocation.getWorld(), previousPointVector, directionVector,
                            0, ((Double) pointLocation.distance(previousLocation)).intValue());
                    while(blocksBetweenPoints.hasNext()){
                        Block block = blocksBetweenPoints.next();
                        if(block.getLocation().distance(playerLocation) <= radius) {
                            edgesBlocks.add(block.getLocation());
                        }
                    }
                    if(i != 0) {
                        previousPoint = point;
                    } else {
                        break;
                    }
                }
            }
        }

        return edgesBlocks;
    }

}