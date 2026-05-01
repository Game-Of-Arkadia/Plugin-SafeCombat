package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.combat.FightStopReason;
import fr.gameofarkadia.safecombat.configuration.PvpConfiguration;
import fr.gameofarkadia.safecombat.protection.ProtectionReason;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Starts and cancel fighting state.
 */
public class FightListener implements Listener {

  private final PvpConfiguration config = Main.config().getPvpConfiguration();

  /**
   * Make player and killer in PvP
   *
   * @param e The event
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onPlayerFightPlayer(@NotNull EntityDamageByEntityEvent e) {
    // Only listen for player to player combat event.
    if (!(e.getEntity() instanceof Player player) || isNPC(player)) return;
    Entity realDamager = e.getDamageSource().getCausingEntity();
    if(!(realDamager instanceof Player damager) || isNPC(damager)) return;

    // Cancel fight if the player or the damager is protected
    if (SafeCombatAPI.isProtected(damager) || SafeCombatAPI.isProtected(player)) {
      if(SafeCombatAPI.isProtected(damager)) {
        damager.sendMessage(Main.prefix() + "§cTu es§2 protégé§c. Tu ne peux donc pas attaquer.");
      } else if(SafeCombatAPI.isProtected(player)) {
        damager.sendMessage(Main.prefix() + "§cCe joueur est§2 protégé§c. Tu ne peux donc pas l'attaquer.");
      }
      e.setCancelled(true);
      return;
    }

    // For each player, update state.
    // Ignore players in creative !
    if (!damager.getGameMode().equals(GameMode.CREATIVE)) {
      SafeCombatAPI.getCombatManager().setStartedFight(damager, false);
    }
    if (!player.getGameMode().equals(GameMode.CREATIVE)) {
      SafeCombatAPI.getCombatManager().setStartedFight(player, true);
    }
  }

  private static boolean isNPC(@NotNull Entity entity) {
    return entity.hasMetadata("NPC");
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onPlayerDeath(@NotNull PlayerDeathEvent e) {
    // On death, clear 'fighting' status.
    SafeCombatAPI.getCombatManager().clearFightStatus(e.getPlayer(), FightStopReason.DEATH);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onPlayerRespawns(@NotNull PlayerRespawnEvent e) {
    // Ignore portals "respawns".
    if (e.getRespawnReason() != PlayerRespawnEvent.RespawnReason.DEATH) return;

    // Respawn protection
    if (config.hasRespawnProtection()) {
      SafeCombatAPI.getProtectionManager().addPlayerProtection(
          e.getPlayer(),
          ProtectionReason.RESPAWN,
          config.getRespawnDuration().duration()
      );
    }
  }
}
