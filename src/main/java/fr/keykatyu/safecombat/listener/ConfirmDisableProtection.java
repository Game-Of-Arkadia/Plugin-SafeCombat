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
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ConfirmDisableProtection implements Listener {

    private final Player player;

    public ConfirmDisableProtection(Player player) {
        this.player = player;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessageReceived(AsyncPlayerChatEvent e) {
        if(!e.getPlayer().equals(player)) return;
        e.setCancelled(true);
        HandlerList.unregisterAll(this);

        if(e.getMessage().equals(Main.getLang().get("protection.cancellation-cancel-code"))) {
            player.sendMessage(Util.prefix() + Main.getLang().get("protection.cancelled"));
        } else if (e.getMessage().equals(Main.getLang().get("protection.cancellation-code"))) {
            Main.getCombatManager().getProtectedPlayers().get(player.getUniqueId()).cancel();
            Main.getCombatManager().getProtectedPlayers().remove(player.getUniqueId());
            player.sendMessage(Util.prefix() + Main.getLang().get("protection.removed"));
        } else {
            player.sendMessage(Util.prefix() + Main.getLang().get("protection.try-again"));
        }
    }

}
