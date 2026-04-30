package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.configuration.PvpConfiguration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * General-purpose listener for some specific features.
 */
public class SafeZoneListener implements Listener {

  private final PvpConfiguration config = Main.config().getPvpConfiguration();

  /**
   * Cooldown riptide tridents for the player if set to true
   * in config.yml
   *
   * @param e The event
   */
  @EventHandler(priority = EventPriority.MONITOR)
  void onPlayerRiptide(@NotNull PlayerRiptideEvent e) {
    if( ! config.isRiptideEnabled()) return;

    ItemStack is = e.getItem();
    if (!is.hasItemMeta()) return;
    if (!is.getItemMeta().hasEnchant(Enchantment.RIPTIDE)) return;
    e.getPlayer().setCooldown(Material.TRIDENT, (int) config.getRespawnDuration().toTicks());
  }

  /**
   * Cooldown ender pearl for the player if set to true
   * in config.yml
   *
   * @param e The event
   */
  @EventHandler(priority = EventPriority.MONITOR)
  void onEnderPearlThrown(@NotNull ProjectileLaunchEvent e) {
    if (!config.isEnderpearlCooldownEnabled()) return;
    Projectile projectile = e.getEntity();
    if (!(projectile instanceof EnderPearl enderPearl) || !(enderPearl.getShooter() instanceof Player player)) return;

    SafeCombatScheduler.run(() -> player.setCooldown(Material.ENDER_PEARL, (int) config.getEnderpearlCooldown().asTicks()));
  }

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
