package fr.keykatyu.safecombat.listener;

import fr.keykatyu.safecombat.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SafeCombatListener implements Listener {

    /**
     * Make player and killer in PvP
     * @param e The event
     */
    @EventHandler
    public void onPlayerFightPlayer(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player killer)) return;
        if(!(e.getEntity() instanceof Player player)) return;
        if(!Main.getCombatManager().isFighting(killer)) Main.getCombatManager().setPlayerFighting(killer);
        if(!Main.getCombatManager().isFighting(player)) Main.getCombatManager().setPlayerFighting(player);
    }

}
