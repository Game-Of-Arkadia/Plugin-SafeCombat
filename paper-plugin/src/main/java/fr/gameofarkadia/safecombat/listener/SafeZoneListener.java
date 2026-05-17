package fr.gameofarkadia.safecombat.listener;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.bridge.WGBridge;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * General-purpose listener for some specific features.
 */
public class SafeZoneListener implements Listener {

  private static final Set<UUID> pushingBack = new HashSet<>();

  @EventHandler
  void zoneSwitchEvent(@NotNull RegionEnteredEvent event) {
    Player player = event.getPlayer();
    if(player == null) return;
    if(pushingBack.contains(player.getUniqueId())) return;

    boolean isSafeZone = event.getRegion().getFlag(Flags.PVP) == StateFlag.State.DENY;
    boolean isFighting = SafeCombatAPI.isFighting(player);
    boolean isBypass = event.getRegion().getFlag(WGBridge.getBypassSafeZone()) == StateFlag.State.ALLOW;

    if(isSafeZone && isFighting && !isBypass) {

      // Push back
      event.setCancelled(true);
      Vector dir = player.getLocation().getDirection();
      dir.setY(0);
      if (dir.lengthSquared() < 1e-4) dir = new Vector(1, 0, 0);
      Location back = player.getLocation().subtract(dir.normalize().multiply(2));
      
      player.setVelocity(new Vector(0, 0, 0));
      pushingBack.add(player.getUniqueId());
      try {
        player.teleport(back);
      } finally {
        pushingBack.remove(player.getUniqueId());
      }
      player.sendMessage(Main.prefix() + "§cVous ne pouvez pas entrer dans une zone sûre tant que vous êtes en combat.");
      player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 0.85f);
    }
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

    // Get first part
    String[] args = e.getMessage().replace("/", "").toLowerCase().split(" ");
    String first;
    if(args[0].contains(":")) {
      first = args[0].split(":", 2)[1];
    } else {
      first = args[0];
    }

    if (Main.config().isBanned(first)) {
      e.setCancelled(true);
      player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
      player.sendMessage(Main.prefix() + "§cCette commande est bannie en combat.");
    }
  }

}
