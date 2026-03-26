package fr.gameofarkadia.safecombat.bridge;

import net.william278.husksync.api.HuskSyncAPI;
import net.william278.husksync.data.DataSnapshot;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to sync Husk-sync data.
 */
public final class HuskSyncHelper {
  private HuskSyncHelper() {}

  private static final Logger LOG = LoggerFactory.getLogger(HuskSyncHelper.class);

  /**
   * Clear a player inventory.
   * @param player player to clear.
   */
  public static void clearInventory(@NotNull OfflinePlayer player) {
    HuskSyncAPI.getInstance().getUser(player.getUniqueId()).thenAccept(optionalUser -> {
      if (optionalUser.isEmpty()) {
        LOG.warn("Internal player not found: {}.", player.getName());
        return;
      }
      var user = optionalUser.get();

      HuskSyncAPI.getInstance().getCurrentData(user).thenAccept(optionalSnapshot -> {
        if (optionalSnapshot.isEmpty()) {
          LOG.warn("No data to remove for {}.", player.getName());
          return;
        }

        // Get the snapshot, which you can then do stuff with
        DataSnapshot.Unpacked snapshot = optionalSnapshot.get();
        snapshot.getInventory().ifPresent(inventory -> {
          inventory.clear();
          HuskSyncAPI.getInstance().setCurrentData(user, snapshot);
          LOG.info("Cleared + saved inventory data for {}.", player.getName());
        });
      });
    });

  }

}
