package fr.keykatyu.safecombat.listener;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.listener.task.PlayerDisconnectedTask;
import fr.keykatyu.safecombat.util.Config;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SafeCombatListener implements Listener {

    /**
     * Make player and killer in PvP
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFightPlayer(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof Player player) || e.getEntity().hasMetadata("NPC")) return;

        Player damager;
        if(e.getDamager() instanceof Arrow arrow) {
            if(!(arrow.getShooter() instanceof Player k)) return;
            damager = k;
        } else if (e.getDamager() instanceof Player p) {
            damager = p;
        } else {
            return;
        }

        // Cancel fight if the player or the damager is protected
        if(Main.getCombatManager().isProtected(damager) || Main.getCombatManager().isProtected(player)) {
            e.setCancelled(true);
            return;
        }

        // Check if players are ally
        FPlayer fKiller = FPlayers.getInstance().getByPlayer(damager);
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if(fKiller.getRelationTo(fPlayer).isAtLeast(Relation.ALLY)) return;

        if(!damager.getGameMode().equals(GameMode.CREATIVE)) {
            if(!Main.getCombatManager().isFighting(damager)) {
                Main.getCombatManager().setPlayerFighting(damager);
            } else {
                Main.getCombatManager().updateInstant(damager);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if(e.getEntity().getKiller() == null) return;
        Main.getDiedPlayers().add(e.getEntity().getName());
    }

    /**
     * Prevent spawn kill by applying a protection
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawns(PlayerRespawnEvent e) {
        if(!e.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.DEATH)) return;
        if(!Main.getDiedPlayers().contains(e.getPlayer().getName())) return;
        Main.getDiedPlayers().remove(e.getPlayer().getName());
        Main.getCombatManager().setPlayerProtected(e.getPlayer(), Instant.now().plus(Config.getInt("pvp.respawn-protection"), ChronoUnit.SECONDS), 20);
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

        // Newbie / PvP protection
        if(player.getLastPlayed() == 0) {
            Main.getCombatManager().setPlayerProtected(player, Instant.now().plus(Config.getInt("pvp.newbie-protection"), ChronoUnit.HOURS), 1200);
        } else if (Main.getCombatManager().isProtected(player)) {
            Main.getCombatManager().getProtectedPlayers().get(player.getUniqueId()).getBossBar().addPlayer(player);
            player.sendMessage(Util.prefix() + Config.getString("messages.protection.join"));
        }

        // Combat disconnection
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
