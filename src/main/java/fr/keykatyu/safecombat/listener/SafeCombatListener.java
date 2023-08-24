package fr.keykatyu.safecombat.listener;

import fr.keykatyu.safecombat.Main;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SafeCombatListener implements Listener {

    /**
     * Make player and killer in PvP
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFightPlayer(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player killer)) return;
        if(!(e.getEntity() instanceof Player player) || e.getEntity().hasMetadata("NPC")) return;

        if(!killer.getGameMode().equals(GameMode.CREATIVE)) {
            if(!Main.getCombatManager().isFighting(killer)) {
                Main.getCombatManager().setPlayerFighting(killer);
            } else {
                Main.getCombatManager().updateInstant(killer);
            }
        }

        if(!player.getGameMode().equals(GameMode.CREATIVE)) {
            if(!Main.getCombatManager().isFighting(player)) {
                Main.getCombatManager().setPlayerFighting(player);
            } else {
                Main.getCombatManager().updateInstant(player);
            }
        }
    }

    /**
     * Kill player if he's fighting
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFightingQuit(PlayerQuitEvent e) {
        if(!Main.getCombatManager().isFighting(e.getPlayer())) return;
        e.setQuitMessage("§6§l" + e.getPlayer().getName() + " §cs'est déconnecté en combat.");
        e.getPlayer().setHealth(0);
    }

}
