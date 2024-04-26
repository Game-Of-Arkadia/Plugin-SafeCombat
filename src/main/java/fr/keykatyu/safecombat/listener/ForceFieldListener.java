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
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayersCombatEnd(PlayerStopsFightingEvent e) {
        Player player = e.getPlayer();
        if(!blocksChangedMap.containsKey(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Set<Location> blocksRemoved;
            if (blocksChangedMap.containsKey(player.getUniqueId())) {
                blocksRemoved = blocksChangedMap.get(player.getUniqueId());
            } else {
                blocksRemoved = new HashSet<>();
            }

            Collection<BlockState> removedBlockStates = new ArrayList<>();
            for (Location location : blocksRemoved) {
                Block block = location.getBlock();
                removedBlockStates.add(block.getState());
            }
            player.sendBlockChanges(removedBlockStates);

            blocksChangedMap.remove(player.getUniqueId());
        });
    }

    /**
     * Send custom block changes if the player
     * tries to go into a safe zone while he's fighting
     * @param e The move event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if(from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
            && from.getBlockZ() == to.getBlockZ()) return;
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player)) return;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Set<Location> blocksToChange = retrieveBlocksToChange(player);
            Set<Location> blocksRemoved;
            if (blocksChangedMap.containsKey(player.getUniqueId())) {
                blocksRemoved = blocksChangedMap.get(player.getUniqueId());
            } else {
                blocksRemoved = new HashSet<>();
            }

            Collection<BlockState> blockStates = new ArrayList<>();
            for (Location location : blocksToChange) {
                BlockState blockState = location.getBlock().getState().copy();
                blockState.setType(Material.RED_STAINED_GLASS);
                blockStates.add(blockState);
                blocksRemoved.remove(location);
            }
            player.sendBlockChanges(blockStates);

            Collection<BlockState> removedBlockStates = new ArrayList<>();
            for (Location location : blocksRemoved) {
                Block block = location.getBlock();
                removedBlockStates.add(block.getState());
            }
            player.sendBlockChanges(removedBlockStates);

            blocksChangedMap.put(player.getUniqueId(), blocksToChange);
        });
    }

    /**
     * Cancel ender pearl throw in safe zone while
     * player is fighting
     * @param e The teleport event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnderPearlTeleport(PlayerTeleportEvent e) {
        if(!e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) return;
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player)) return;
        Location to = e.getTo();
        if(Main.isWGEnabled() && !WGBridge.isSafeZoneAt(player, to)) return;
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        e.setCancelled(true);
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
        Location location1 = playerLoc.clone().add(radius, 0, radius);
        Location location2 = playerLoc.clone().subtract(radius, 0, radius);

        int topX = Math.max(location1.getBlockX(), location2.getBlockX());
        int bottomX = Math.min(location1.getBlockX(), location2.getBlockX());
        int topZ = Math.max(location1.getBlockZ(), location2.getBlockZ());
        int bottomZ = Math.min(location1.getBlockZ(), location2.getBlockZ());

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                Location location = new Location(playerLoc.getWorld(), x, playerLoc.getY(), z);
                if (Main.isWGEnabled() && !WGBridge.isSafeZoneAt(player, location)) continue;

                for (int y = -height; y < height; y++) {
                    Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                    loc.setY(loc.getY() + y);
                    if (!loc.getBlock().getType().equals(Material.AIR) && !(loc.getBlock().getBlockData() instanceof Waterlogged)) continue;
                    blocksToChange.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                }
            }
        }
        return blocksToChange;
    }



}