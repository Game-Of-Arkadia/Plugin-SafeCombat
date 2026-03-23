package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class ConfirmDisableProtection implements Listener {

    private final Player player;

    public ConfirmDisableProtection(Player player) {
        this.player = player;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onMessageReceived(@NotNull AsyncPlayerChatEvent e) {
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
