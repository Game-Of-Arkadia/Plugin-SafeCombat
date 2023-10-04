package fr.keykatyu.safecombat.listener;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.listener.task.PlayerDisconnectedTask;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SafeCombatListener implements Listener {

    /**
     * Make player and killer in PvP
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFightPlayer(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player killer)) return;
        if(!(e.getEntity() instanceof Player player) || e.getEntity().hasMetadata("NPC")) return;

        // Check if players are ally
        FPlayer fKiller = FPlayers.getInstance().getByPlayer(killer);
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if(fKiller.getRelationTo(fPlayer).isAtLeast(Relation.ALLY)) return;

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
     * Make player and killer in PvP if it's a bow shoot
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFightPlayerBow(EntityShootBowEvent e) {
        Arrow a = (Arrow) e.getProjectile();
        if(!(a.getShooter() instanceof Player killer)) return;
        if(!(e.getEntity() instanceof Player player) || e.getEntity().hasMetadata("NPC")) return;

        // Check if players are ally
        FPlayer fKiller = FPlayers.getInstance().getByPlayer(killer);
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if(fKiller.getRelationTo(fPlayer).isAtLeast(Relation.ALLY)) return;

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
