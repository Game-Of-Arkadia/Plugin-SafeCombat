package fr.gameofarkadia.safecombat.listener;

import fr.gameofarkadia.arkadialib.api.utils.Ref;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.configuration.PvpConfiguration;
import fr.gameofarkadia.safecombat.protection.ProtectionReason;
import net.william278.husksync.api.HuskSyncAPI;
import net.william278.husksync.data.Data;
import net.william278.husksync.event.BukkitPreSyncEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

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
    if(SafeCombatAPI.isWantedLocally(player)) {
      Main.logger().info("Reconnected while still wanted ({}). Clearing wanted status.", player.getName());
      SafeCombatAPI.getWantedPlayersManager().reconnected(player);
      return;
    }
    if(SafeCombatAPI.isWanted(player)) {
      // Nothing to do : let the task handle it.
      Main.logger().info("Player {} is wanted in another server. Letting the task handle him.", player.getName());
      return;
    }

    // Newbie / PvP protection
    Ref<Boolean> isFirstRef = new Ref<>();
    Main.firstPlayerConnectionHandler().checkFirstPlayerConnection(player)
        .thenCompose(isFirst -> {
          isFirstRef.object = isFirst;
          return SafeCombatAPI.getProtectionManager().signalPlayerJoined(player);
        }).thenRun(() -> {
          if(!SafeCombatAPI.isProtected(player))
            applyConnectionProtection(player, isFirstRef.object);
        })
        .exceptionally(err -> {
          Main.logger().error("An error occurred while checking player {} first connection or signaling his join.", player.getName(), err);
          return null;
        });
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void huskSyncLoad(@NotNull BukkitPreSyncEvent event) {
    Main.logger().info("PRE SYNC HUSK-SYNC EVENT.");

    UUID uuid = event.getUser().getUuid();
    Main.getToClearPlayersList().shouldClear(uuid).thenApply(shouldClear -> {
      Main.logger().info("SHOULD CLEAR ? {}", shouldClear);
      if(shouldClear) {
        Main.logger().info("Player {} should have his inventory cleared. Clearing it now.", uuid);
        System.out.println("before edit");
        try {
          event.editData(data -> {
            try {
              var inv = data.getInventory().orElse(null);
              if (inv != null) inv.clear();
              System.out.println("ok cleared");
            } catch (Throwable e) {
              e.printStackTrace();
            }
          });
        } catch (Throwable e) {
          e.printStackTrace();
        }
        System.out.println("done clear");
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) player.getInventory().clear();
        //HuskSyncAPI.getInstance().createSnapshot(event.getUser());
      }
      return null;
    }).exceptionally(err -> {
      Main.logger().error("An error occurred while checking if player {} should have his inventory cleared.", uuid, err);
      return null;
    });
  }

  private void applyConnectionProtection(@NotNull Player player, boolean firstConnection) {
    if (firstConnection) {
      var newbieDuration = config.getNewbieProtectionDuration();
      Main.logger().info("Applying NEWBIE protection to player {} for {} seconds.", player.getName(), newbieDuration.toSecondsInt());
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
      Main.logger().info("Applying server join protection to player {} for {} seconds.", player.getName(), connectDuration.toSecondsInt());
      SafeCombatAPI.getProtectionManager().addPlayerProtection(
          player,
          ProtectionReason.SERVER_JOIN,
          connectDuration.asJavaDuration()
      );
    }
  }

}
