package fr.keykatyu.safecombat.listener;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.listener.task.PlayerDisconnectedTask;
import fr.keykatyu.safecombat.util.Config;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

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

    /**
     * Remove died player from fight mode and add him to the list
     * for respawn protection verification
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if(Main.getCombatManager().isFighting(player)) {
            Main.getCombatManager().getFightingPlayers().get(player.getName()).cancel();
        }
        if(player.getKiller() == null) return;
        Main.getDiedPlayers().add(e.getEntity().getName());
    }

    /**
     * Prevent spawn kill by applying a protection
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawns(PlayerRespawnEvent e) {
        if(!e.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.DEATH)) return;
        Player player = e.getPlayer();
        if(!Main.getDiedPlayers().contains(player.getName())) return;
        Main.getDiedPlayers().remove(player.getName());
        Main.getCombatManager().setPlayerProtected(player, Instant.now().plus(Config.getInt("pvp.respawn-protection"), ChronoUnit.SECONDS), 20);
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

    /**
     * Cooldown riptide tridents for the player if set to true
     * in config.yml
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRiptide(PlayerRiptideEvent e) {
        if(!Config.getBoolean("pvp.trident.riptide-cooldown")) return;
        ItemStack is = e.getItem();
        if(!is.hasItemMeta()) return;
        if(!is.getItemMeta().hasEnchant(Enchantment.RIPTIDE)) return;
        e.getPlayer().setCooldown(Material.TRIDENT, Config.getInt("pvp.trident.cooldown-time") * 20);
    }

    /**
     * Cooldown ender pearl for the player if set to true
     * in config.yml
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderPearlThrown(ProjectileLaunchEvent e) {
        if(!Config.getBoolean("pvp.enderpearl.custom-cooldown")) return;
        Projectile projectile = e.getEntity();
        if(!(projectile instanceof EnderPearl enderPearl)) return;
        if(!(enderPearl.getShooter() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.setCooldown(Material.ENDER_PEARL, Config.getInt("pvp.enderpearl.cooldown-time") * 20), 1);
    }

}
