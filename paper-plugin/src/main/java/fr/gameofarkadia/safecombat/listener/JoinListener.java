package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.configuration.PvpConfiguration;
import fr.gameofarkadia.safecombat.protection.ProtectionReason;
import fr.gameofarkadia.safecombat.util.FirstJoinHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handle server joins. <br/>
 * Apply eventual protections.
 */
public class JoinListener implements Listener {

  private final PvpConfiguration config = Main.config().getPvpConfiguration();

  /**
   * Called when a player joins after he is in the kill list
   *
   * @param e The event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  void onPlayerJoins(@NotNull PlayerJoinEvent e) {
    Player player = e.getPlayer();

    // Is wanted ?
    if(SafeCombatAPI.isWanted(player)) {
      // Nothing to do : let the task handle it.
      Main.logger().info("Player {} is wanted in another server. Letting the task handle him.", player.getName());
      return;
    }

    // Newbie / PvP protection
    FirstJoinHelper.isItFirstConnection(player).whenComplete((isFirst, err) -> {
      if (err != null) {
        Main.logger().error("Could not check if player {} is in first connection. No protection will be applied.", player.getName(), err);
        return;
      }

      // First login : add a newbie protection
      applyConnectionProtection(player, isFirst);

      // Not first connection. Signal join.
      // This will update only if needed.
      SafeCombatAPI.getProtectionManager().signalPlayerJoined(player);
    });
  }

  private void applyConnectionProtection(@NotNull Player player, boolean firstConnection) {
    if (firstConnection) {
      var newbieDuration = config.getNewbieProtectionDuration();
      if (newbieDuration.asTicks() > 0) {
        SafeCombatAPI.getProtectionManager().addPlayerProtection(
            player,
            ProtectionReason.NEW_PLAYER,
            newbieDuration.asJavaDuration()
        );
      }
      return;
    }

    var connectDuration = config.getServerJoinProtectionDuration();
    if (connectDuration.asTicks() > 0) {
      SafeCombatAPI.getProtectionManager().addPlayerProtection(
          player,
          ProtectionReason.SERVER_JOIN,
          connectDuration.asJavaDuration()
      );
    }
  }

}
