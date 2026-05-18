package fr.gameofarkadia.safecombat.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.bridge.WGBridge;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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

  @EventHandler
  void onRegionLeave(@NotNull RegionLeftEvent event) {
    Player player = event.getPlayer();
    if(player == null) return;
    if(!SafeCombatAPI.isFighting(player)) return;

    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();
    Location loc = BukkitAdapter.adapt(player.getLocation());

    StateFlag.State destinationPvpState = query.queryState(loc, null, Flags.PVP);
    StateFlag.State destinationBypassState = query.queryState(loc, null, WGBridge.getBypassSafeZone());

    boolean goingToSafeZone = destinationPvpState == StateFlag.State.DENY;
    boolean hasDestinationBypass = destinationBypassState == StateFlag.State.ALLOW;

    if(goingToSafeZone && !hasDestinationBypass) {
      event.setCancelled(true);
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
