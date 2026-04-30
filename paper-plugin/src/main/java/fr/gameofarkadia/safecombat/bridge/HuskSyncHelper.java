package fr.gameofarkadia.safecombat.bridge;

import fr.gameofarkadia.safecombat.Main;
import net.william278.husksync.api.HuskSyncAPI;
import net.william278.husksync.data.BukkitData;
import net.william278.husksync.data.DataSnapshot;
import net.william278.husksync.user.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Helper to sync Husk-sync data.
 */
public final class HuskSyncHelper {
  private HuskSyncHelper() {}

  private static final Logger LOG = LoggerFactory.getLogger(HuskSyncHelper.class);

  /**
   * Save a player's inventory.
   * @param player player to save.
   */
  public static void savePlayerInventory(@NotNull Player player) {
    HuskSyncAPI.getInstance().getOnlineUser(player.getUniqueId()).ifPresent(user -> {
      var snapshot = HuskSyncAPI.getInstance().createSnapshot(user);
      HuskSyncAPI.getInstance().addSnapshot(user, snapshot);
    });
  }

  /**
   * Clear a player inventory.
   * @param player player to clear.
   */
  public static @NotNull CompletableFuture<List<ItemStack>> clearInventory(@NotNull OfflinePlayer player) {
    Ref<User> userRef = new Ref<>();

    // Get User from UUID
    return HuskSyncAPI.getInstance().getUser(player.getUniqueId())
        .thenApply(optionalUser -> {
              if (optionalUser.isEmpty()) {
                LOG.warn("Internal player not found: {}.", player.getName());
                return null;
              }
              return optionalUser.get();
            })

        // Get data of user
        .thenCompose(user -> {
          if(user == null) return CompletableFuture.completedFuture(Optional.empty());
          userRef.object = user;
          return HuskSyncAPI.getInstance().getCurrentData(user);
        })

        // Transform data
        .thenApply(optionalSnapshot -> {
          if (optionalSnapshot.isEmpty()) {
            LOG.warn("No data to remove for {}.", player.getName());
            return List.of();
          }

          // Get the snapshot, which you can then do stuff with
          DataSnapshot.Unpacked snapshot = optionalSnapshot.get();
          List<ItemStack> output = new ArrayList<>();
          if(snapshot.getInventory().isPresent()) {
            var inventory = snapshot.getInventory().get();

            // Save items
            if(inventory instanceof BukkitData.Items.Inventory bukkitInventory) {
              Arrays.stream(bukkitInventory.getContents()).forEach(itemStack -> {
                if(itemStack != null)
                  output.add(itemStack.clone());
              });
            } else {
              Main.logger().error("Inventory is not a Bukkit inventory, cannot clear it. Type: '{}'.", inventory.getClass());
            }

            // Clear and persist.
            inventory.clear();
            HuskSyncAPI.getInstance().setCurrentData(userRef.object, snapshot);
            HuskSyncAPI.getInstance().addSnapshot(userRef.object, snapshot);
          }
          return output;
        });
  }

  private static class Ref<T> {
    T object;
  }

  /**
   * Get the registered server ID.
   * @return a non-null server ID.
   */
  public static @NotNull String getServerId() {
    return HuskSyncAPI.getInstance().getPlugin().getServerName();
  }

}
