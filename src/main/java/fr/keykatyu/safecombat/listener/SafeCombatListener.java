package fr.keykatyu.safecombat.listener;

import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.listener.task.PlayerDisconnectedTask;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
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
        if(Main.getKickedPlayers().contains(e.getPlayer().getName())) return;
        Main.getInstance().getServer().getPluginManager().registerEvents(new PlayerDisconnectedTask(e.getPlayer()), Main.getInstance());
    }

    /**
     * Called when a player joins after he is in the kill list
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoins(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if(!Main.getCombatManager().getPlayersToKill().contains(player.getName())) return;
        Main.getCombatManager().getPlayersToKill().remove(player.getName());
        e.setJoinMessage("§6§l" + player.getName() + " §es'est reconnecté après sa déconnexion en combat.");
        player.getInventory().clear();
        player.setHealth(0);
    }

    /**
     * Filter kicked players/server restart
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKicked(PlayerKickEvent e) {
        Main.getKickedPlayers().add(e.getPlayer().getName());
    }

}
