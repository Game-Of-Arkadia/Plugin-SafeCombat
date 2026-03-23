package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.arkadialib.api.utils.DurationUtils;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.events.PlayerStartsFightingEvent;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import fr.gameofarkadia.safecombat.listener.task.PlayerDisconnectedTask;
import fr.gameofarkadia.safecombat.util.Config;
import fr.gameofarkadia.safecombat.util.Util;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

public class SafeCombatListener implements Listener {

    /**
     * Make player and killer in PvP
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onPlayerFightPlayer(@NotNull EntityDamageByEntityEvent e) {
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

        if(!damager.getGameMode().equals(GameMode.CREATIVE)) {
            if(!Main.getCombatManager().isFighting(damager)) {
                Main.getCombatManager().setPlayerFighting(damager);
                Bukkit.getPluginManager().callEvent(new PlayerStartsFightingEvent(damager, PlayerStartsFightingEvent.Type.ATTACKER));
            } else {
                Main.getCombatManager().updateInstant(damager);
            }
        }

        if(!player.getGameMode().equals(GameMode.CREATIVE)) {
            if(!Main.getCombatManager().isFighting(player)) {
                Main.getCombatManager().setPlayerFighting(player);
                Bukkit.getPluginManager().callEvent(new PlayerStartsFightingEvent(player, PlayerStartsFightingEvent.Type.ATTACKED));
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
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerDeath(@NotNull PlayerDeathEvent e) {
        Player player = e.getEntity();
        Main.getCombatManager().removeFromFighting(player);
        if(player.getKiller() == null) return;
        Main.getDiedPlayers().add(e.getEntity().getUniqueId());
    }

    /**
     * Prevent spawn kill by applying a protection
     * @param e The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerRespawns(@NotNull PlayerRespawnEvent e) {
        if(!e.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.DEATH)) return;
        Player player = e.getPlayer();
        if(!Main.getDiedPlayers().contains(player.getUniqueId())) return;
        Main.getDiedPlayers().remove(player.getUniqueId());
        int respawnProtection = Config.getInt("pvp.respawn-protection");
        if(respawnProtection > 0) {
           Main.getCombatManager().addPlayerProtection(player, Duration.ofSeconds(respawnProtection));
        }
    }

    /**
     * Kill player if he's fighting
     * @param e The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerFightingQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player)) return;
        if(Main.getKickedPlayers().contains(player.getUniqueId())) return;
        int duration = Config.getInt("pvp.disconnection");
        if(duration < 0) {
            Bukkit.broadcast(Component.text("§6§l" + player.getName() + " §c" + Main.getLang().get("fight.player-disconnected")
                .replace("%duration%", DurationUtils.formatDuration(Duration.ofSeconds(duration)))));

            if(Main.getCombatManager().removeFromFighting(player)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> Bukkit.getPluginManager().callEvent(new PlayerStopsFightingEvent(player)));
            }

            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                for(ItemStack itemStack : player.getInventory().getContents()) {
                    if(itemStack == null) continue;
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                }
            });
        } else {
            Main.getInstance().getSLF4JLogger().warn("Player {} shall be killed !", player.getName());
            Bukkit.getPluginManager().registerEvents(new PlayerDisconnectedTask(player, duration), Main.getInstance());
        }
    }

    /**
     * Called when a player joins after he is in the kill list
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerJoins(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Newbie / PvP protection
        if(player.getLastPlayed() == 0) {
            Main.getCombatManager().addPlayerProtection(player, Duration.ofHours(Config.getInt("pvp.newbie-protection")));
        } else if (Main.getCombatManager().isProtected(player)) {
            Main.getCombatManager().getProtectedPlayers().get(player.getUniqueId()).getBossBar().addPlayer(player);
            player.sendMessage(Util.prefix() + Main.getLang().get("protection.join"));
        }

        // Combat disconnection
        Main.getInstance().getSLF4JLogger().info("Player {} has joined the server. Is in kill-list ? {}.", player.getName(), Main.getCombatManager().getPlayersToKill().contains(player.getUniqueId()));
        if(Main.getCombatManager().getPlayersToKill().contains(player.getUniqueId())) {
            Bukkit.broadcast(Component.text("§6§l" + player.getName() + " §e" + Main.getLang().get("fight.player-reconnected")));
            Main.getCombatManager().removePlayerToKill(player);

            player.getInventory().clear();
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                // Inventory has been dropped by disconnect-task. Now, we clear the inventory.
                player.setHealth(0);
            }, 5L);
        }
    }

    /**
     * Filter kicked players/server restart
     * @param e The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerKicked(@NotNull PlayerKickEvent e) {
        Main.getKickedPlayers().add(e.getPlayer().getUniqueId());
    }

    /**
     * Cooldown riptide tridents for the player if set to true
     * in config.yml
     * @param e The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerRiptide(@NotNull PlayerRiptideEvent e) {
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
    @EventHandler(priority = EventPriority.MONITOR)
    void onEnderPearlThrown(@NotNull ProjectileLaunchEvent e) {
        if(!Config.getBoolean("pvp.enderpearl.custom-cooldown")) return;
        Projectile projectile = e.getEntity();
        if(!(projectile instanceof EnderPearl enderPearl)) return;
        if(!(enderPearl.getShooter() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.setCooldown(Material.ENDER_PEARL, Config.getInt("pvp.enderpearl.cooldown-time") * 20), 1);
    }

    /**
     * Cancel command if the player is in pvp
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onPlayerEntersCommand(@NotNull PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player)) return;
        List<String> bannedCommands = Config.getStringList("banned-commands");
        String command = e.getMessage().replace("/", "");
        if(!bannedCommands.contains(command)) return;
        e.setCancelled(true);
        player.sendMessage(Util.prefix() + Main.getLang().get("fight.command-banned"));
    }

}
