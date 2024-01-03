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

package fr.keykatyu.safecombat.listener.task;

import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDisconnectedTask implements Runnable, Listener {

    private final int taskId;
    private final Player player;
    private final Location location;
    private final ItemStack[] items;

    public PlayerDisconnectedTask(Player player) {
        this.player = player;
        location = player.getLocation();
        items = player.getInventory().getContents();
        taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), this, Config.getInt("pvp.disconnection") * 20).getTaskId();
    }

    /**
     * Cancel the task if the player rejoins
     * @param e The event
     */
    @EventHandler
    public void onPlayerJoins(PlayerJoinEvent e) {
        if(!e.getPlayer().getName().equals(player.getName())) return;
        cancel();
    }

    @Override
    public void run() {
        player.getServer().broadcastMessage("§6§l" + player.getName() + " §c" + Main.getLang().get("fight.player-disconnected"));
        Main.getCombatManager().getPlayersToKill().add(player.getName());
        if( Main.getCombatManager().getFightingPlayers().containsKey(player.getName())) {
            Main.getCombatManager().getFightingPlayers().get(player.getName()).cancel();
            Main.getCombatManager().getFightingPlayers().remove(player.getName());
        }

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            for(ItemStack itemStack : items) {
                if(itemStack == null) continue;
                player.getWorld().dropItem(location, itemStack);
            }
        });
        cancel();
    }

    private void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
        HandlerList.unregisterAll(this);
    }

}