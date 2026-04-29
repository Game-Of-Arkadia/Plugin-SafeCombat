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
public class DisconnectCombatListener implements Listener {

  private final PvpConfiguration config = Main.config().getPvpConfiguration();

  /**
   * Kill player if he's fighting
   *
   * @param e The event
   */
  @EventHandler(priority = EventPriority.MONITOR)
  void onPlayerFightingQuit(@NotNull PlayerQuitEvent e) {
    Player player = e.getPlayer();
    if (!Main.getCombatManager().isFighting(player)) return;
    if(e.getReason() != PlayerQuitEvent.QuitReason.DISCONNECTED) {
      Main.logger().info("Player {} was kicked while fighting, ignoring disconnect punishment.", player.getName());
      return;
    }

    // No punishment : we just stop here.
    if (!config.hasDisconnectPunishment()) {
      // Send an event if needed.
      if (Main.getCombatManager().removeFromFighting(player)) {
        Bukkit.getPluginManager().callEvent(new PlayerStopsFightingEvent(player, PlayerStopsFightingEvent.Reason.DISCONNECT));
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

}
