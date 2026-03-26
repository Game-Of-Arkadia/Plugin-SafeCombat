package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.bridge.HuskSyncHelper;
import fr.gameofarkadia.safecombat.configuration.PvpConfiguration;
import fr.gameofarkadia.safecombat.events.PlayerStartsFightingEvent;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
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

/**
 * General-purpose listener for all the main plugin features.
 */
public class SafeCombatListener implements Listener {

    private final PvpConfiguration config = Main.config().getPvpConfiguration();

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

        // For each player, update state. Ignore players in creative !
        if(!damager.getGameMode().equals(GameMode.CREATIVE)) {
            if(Main.getCombatManager().playerIsFighting(damager)) {
                Bukkit.getPluginManager().callEvent(new PlayerStartsFightingEvent(damager, PlayerStartsFightingEvent.Type.ATTACKER));
            }
        }
        if(!player.getGameMode().equals(GameMode.CREATIVE)) {
            if(Main.getCombatManager().playerIsFighting(player)) {
                Bukkit.getPluginManager().callEvent(new PlayerStartsFightingEvent(player, PlayerStartsFightingEvent.Type.ATTACKED));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerDeath(@NotNull PlayerDeathEvent e) {
        Main.getCombatManager().removeFromFighting(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerRespawns(@NotNull PlayerRespawnEvent e) {
        if(!e.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.DEATH)) return;

        // Respawn protection
        if(config.hasRespawnProtection()) {
           Main.getCombatManager().addPlayerProtection(e.getPlayer(), config.getRespawnDuration().duration());
        }
    }

    /**
     * Kill player if he's fighting
     * @param e The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerFightingQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player) || e.getReason() == PlayerQuitEvent.QuitReason.KICKED) return;

        // No punishment : we just stop here.
        if(!config.hasDisconnectPunishment()) {
            if(Main.getCombatManager().removeFromFighting(player)) {
                SafeCombatScheduler.run(() -> Bukkit.getPluginManager().callEvent(new PlayerStopsFightingEvent(player)));
            }
            return;
        }

        // A punishment should be applied !
        // But only after some duration.
        var duration = config.getDurationBeforePunishment();
        Bukkit.broadcast(Component.text("§6§l" + player.getName() + " §c" + Main.getLang().get("fight.player-disconnected")
            .replace("%duration%", duration.print())));
        Main.getCombatManager().startPlayerDisconnectTask(player, duration);
    }

    /**
     * Called when a player joins after he is in the kill list
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerJoins(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Newbie / PvP protection
        Main.getCombatManager().isItFirstConnection(player).whenComplete((isFirst, err) -> {
            if(err != null) {
                Main.logger().error("Could not check if player {} is in first connection. No protection will be applied.", player.getName(), err);
                return;
            }

            // First login : add a newbie protection
            if(isFirst) {
                var newbieDuration = config.getNewbieProtectionDuration();
                if(newbieDuration.asTicks() > 0) {
                    Main.getCombatManager().addPlayerProtection(player, newbieDuration.asJavaDuration());
                }
                return;
            }

            // Not first connection. Are we protected ? If yeas, display boss-bar.
            if (Main.getCombatManager().isProtected(player)) {
                Main.getCombatManager().getProtectedTask(player).updatePlayer(player);
                player.sendMessage(Util.prefix() + Main.getLang().get("protection.join"));
            }
        });

        // Check for reconnect after disconnecting during fight
        Main.logger().info("Player {} has joined the server. Is in kill-list ? {}.", player.getName(), Main.getCombatManager().shouldBeKilled(player));
        if(Main.getCombatManager().shouldBeKilled(player)) {
            Bukkit.broadcast(Component.text("§6§l" + player.getName() + " §e" + Main.getLang().get("fight.player-reconnected")));

            player.getInventory().clear();
            HuskSyncHelper.clearInventory(player);

            SafeCombatScheduler.runLater(() -> {
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
        if(!config.isEnderpearlCooldownEnabled()) return;
        Projectile projectile = e.getEntity();
        if(!(projectile instanceof EnderPearl enderPearl) || !(enderPearl.getShooter() instanceof Player player)) return;

        SafeCombatScheduler.run(() -> player.setCooldown(Material.ENDER_PEARL, (int) config.getEnderpearlCooldown().asTicks()));
    }

    /**
     * Cancel command if the player is in pvp
     * @param e The event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onPlayerEntersCommand(@NotNull PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if(!Main.getCombatManager().isFighting(player)) return;
        String command = e.getMessage().replace("/", "");

        if(Main.config().isBanned(command)) {
            e.setCancelled(true);
            player.sendMessage(Util.prefix() + Main.getLang().get("fight.command-banned"));
        }
    }

}
