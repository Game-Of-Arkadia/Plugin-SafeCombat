package fr.keykatyu.safecombat.listener;

import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.util.Config;
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
        switch (e.getMessage()) {
            case "cancel" -> {
                player.sendMessage(Util.prefix() + Config.getString("messages.protection.cancelled"));
            }
            case "Je souhaite ne plus bénéficier de ma protection" -> {
                Main.getCombatManager().getProtectedPlayers().get(player.getUniqueId()).cancel();
                Main.getCombatManager().getProtectedPlayers().remove(player.getUniqueId());
                player.sendMessage(Util.prefix() + Config.getString("messages.protection.removed"));
            }
            default -> {
                player.sendMessage(Util.prefix() + Config.getString("messages.protection.try-again"));
            }
        }
    }

}
