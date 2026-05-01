package fr.gameofarkadia.safecombat.listener;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.bridge.WGBridge;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import net.raidstone.wgevents.events.RegionEnteredEvent;

/**
 * General-purpose listener for some specific features.
 */
public class SafeZoneListener implements Listener {

  @EventHandler
  void zoneSwitchEvent(@NotNull RegionEnteredEvent event) {
    Player player = event.getPlayer();
    if(player == null) return;

    boolean isSafeZone = event.getRegion().getFlag(Flags.PVP) == StateFlag.State.DENY;
    boolean isFighting = SafeCombatAPI.isFighting(player);
    boolean isBypass = event.getRegion().getFlag(WGBridge.getBypassSafeZone()) == StateFlag.State.ALLOW;

    if(isSafeZone && isFighting && !isBypass) {
      event.setCancelled(true);
      player.sendMessage(Main.prefix() + "§cVous ne pouvez pas entrer dans une zone sûre tant que vous êtes en combat.");
      player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 0.85f);
    }
  }


  //FIXME mettre ça dan combatbehavior !

//  /**
//   * Cooldown riptide tridents for the player if set to true
//   * in config.yml
//   *
//   * @param e The event
//   */
//  @EventHandler(priority = EventPriority.MONITOR)
//  void onPlayerRiptide(@NotNull PlayerRiptideEvent e) {
//    if( ! config.isRiptideEnabled()) return;
//
//    ItemStack is = e.getItem();
//    if (!is.hasItemMeta()) return;
//    if (!is.getItemMeta().hasEnchant(Enchantment.RIPTIDE)) return;
//    e.getPlayer().setCooldown(Material.TRIDENT, (int) config.getRespawnDuration().toTicks());
//  }
//
//  /**
//   * Cooldown ender pearl for the player if set to true
//   * in config.yml
//   *
//   * @param e The event
//   */
//  @EventHandler(priority = EventPriority.MONITOR)
//  void onEnderPearlThrown(@NotNull ProjectileLaunchEvent e) {
//    if (!config.isEnderpearlCooldownEnabled()) return;
//    Projectile projectile = e.getEntity();
//    if (!(projectile instanceof EnderPearl enderPearl) || !(enderPearl.getShooter() instanceof Player player)) return;
//
//    SafeCombatScheduler.run(() -> player.setCooldown(Material.ENDER_PEARL, (int) config.getEnderpearlCooldown().asTicks()));
//  }

  /**
   * Cancel command if the player is in pvp
   *
   * @param e The event
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onPlayerEntersCommand(@NotNull PlayerCommandPreprocessEvent e) {
    Player player = e.getPlayer();
    if (!SafeCombatAPI.isFighting(player)) return;
    String command = e.getMessage().replace("/", "");

    if (Main.config().isBanned(command)) {
      e.setCancelled(true);
      player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
      player.sendMessage(Main.prefix() + "§cCette commande est bannie en combat.");
    }
  }

}
