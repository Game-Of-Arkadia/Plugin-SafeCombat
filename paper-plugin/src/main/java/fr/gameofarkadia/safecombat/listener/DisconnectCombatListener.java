package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.arkadialib.api.utils.DurationUtils;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.combat.FightStopReason;
import fr.gameofarkadia.safecombat.configuration.PvpConfiguration;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
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
    if (!SafeCombatAPI.isFighting(player)) return;

    if(e.getReason() != PlayerQuitEvent.QuitReason.DISCONNECTED) {
      Main.logger().info("Player {} was kicked while fighting, ignoring disconnect punishment.", player.getName());
      return;
    }

    // No punishment : we just stop here.
    if (!config.hasDisconnectPunishment()) {
      SafeCombatAPI.getCombatManager().clearFightStatus(player, FightStopReason.DISCONNECT);
      return;
    }

    // A punishment should be applied !
    // But only after some duration.
    var duration = config.getDurationBeforePunishment();
    Bukkit.broadcast(Component.text(Main.prefix() + "§7Le joueur §4" + player.getName() + "§7 s'est déconnecté en combat. Il a §c" + DurationUtils.formatDuration(duration.duration()) + "§7 pour se reconnecter."));
    SafeCombatAPI.getWantedPlayersManager().declareWanted(player);
  }

}
