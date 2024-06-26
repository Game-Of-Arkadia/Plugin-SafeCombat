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

package fr.keykatyu.safecombat.listener;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.bridge.WGBridge;
import fr.keykatyu.safecombat.listener.event.PlayerStopsFightingEvent;
import fr.keykatyu.safecombat.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class ForceFieldListener implements Listener {

    private final Map<UUID, Set<Location>> blocksChangedMap = new HashMap<>();

    /**
     * Cancels old modified blocks when combat stops
     * @param e The SafeCombat's event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersCombatEnd(PlayerStopsFightingEvent e) {
        Player player = e.getPlayer();
        if(!blocksChangedMap.containsKey(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Set<Location> blocksRemoved = blocksChangedMap.getOrDefault(player.getUniqueId(), new HashSet<>());
            sendRemovedBlocksChanges(player, blocksRemoved);
            blocksChangedMap.remove(player.getUniqueId());
        });
    }

    /**
     * Send custom block changes if the player
     * tries to go into a safe zone while he's fighting
     * @param e The move event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if(from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
            && from.getBlockZ() == to.getBlockZ()) return;
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player)) return;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Set<Location> blocksToChange = retrieveBlocksToChange(player);
            Set<Location> blocksRemoved = blocksChangedMap.getOrDefault(player.getUniqueId(), new HashSet<>());

            Collection<BlockState> blockStates = new ArrayList<>();
            for (Location location : blocksToChange) {
                BlockState blockState = location.getBlock().getState();
                blockState.setType(Material.getMaterial(Config.getString("pvp.forcefield.material")));
                blockStates.add(blockState);
                blocksRemoved.remove(location);
            }
            player.sendBlockChanges(blockStates);
            sendRemovedBlocksChanges(player, blocksRemoved);
            blocksChangedMap.put(player.getUniqueId(), blocksToChange);
        });
    }

    /**
     * Cancel ender pearl throw in safe zone while
     * player is fighting
     * @param e The teleport event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderPearlTeleport(PlayerTeleportEvent e) {
        if(Config.getBoolean("pvp.enderpearl.back-safezone")) return;
        if(!e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) return;
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player)) return;
        Location to = e.getTo();
        if(Main.isWGEnabled() && !WGBridge.isSafeZoneAt(player, to)) return;
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        e.setCancelled(true);
    }

    /**
     * Send to player blocks removed data
     * @param player The player
     * @param blocksRemoved The blocks
     */
    public void sendRemovedBlocksChanges(Player player, Set<Location> blocksRemoved) {
        Collection<BlockState> removedBlockStates = new ArrayList<>();
        for(Location location : blocksRemoved) {
            removedBlockStates.add(location.getBlock().getState());
        }
        player.sendBlockChanges(removedBlockStates);
    }

    /**
     * Retrieve blocks to change
     * @param player The event
     * @return A Set of locations corresponding to block locations
     */
    private Set<Location> retrieveBlocksToChange(Player player) {
        Set<Location> blocksToChange = new HashSet<>();
        int radius = Config.getInt("pvp.forcefield.radius");
        int height = Config.getInt("pvp.forcefield.height");
        Location playerLoc = player.getLocation();

        // Get all blocks (in the radius) around the player
        List<BlockVector2> blocksAround = new ArrayList<>();
        for(int x = playerLoc.getBlockX() - radius; x <= playerLoc.getBlockX() + radius; x++) {
            for(int z = playerLoc.getBlockZ() - radius; z <= playerLoc.getBlockZ() + radius; z++) {
                blocksAround.add(BlockVector2.at(x, z));
            }
        }

        // For each safe zone found around the player
        for(ProtectedRegion safeZone : WGBridge.getSafeZonesInBlocks(player, blocksAround)) {
            // Get the edges of the safe zone
            List<Location> edgesBlocks = WGBridge.retrieveEdgesBlocks(safeZone, playerLoc, radius);
            // +/- height
            for(Location edge : edgesBlocks) {
                for (int y = -height; y < height; y++) {
                    Location loc = edge.clone();
                    loc.setY(loc.getY() + y);
                    Block block = loc.getBlock();
                    if (!block.isEmpty() && !(block.getBlockData() instanceof Waterlogged) && !block.isPassable()) continue;
                    blocksToChange.add(loc);
                }
            }
        }

        return blocksToChange;
    }

}