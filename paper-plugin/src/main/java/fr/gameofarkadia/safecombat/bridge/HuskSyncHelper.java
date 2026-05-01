package fr.gameofarkadia.safecombat.bridge;

import fr.gameofarkadia.safecombat.Main;
import net.william278.husksync.api.HuskSyncAPI;
import net.william278.husksync.data.BukkitData;
import net.william278.husksync.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
   * Clear a player inventory. Return the complete inventory content as Bukkit data.
   * @param player player to clear.
   * @return a future with the inventory content as Bukkit data, or an empty list if no data to clear.
   */
  public static @NotNull CompletableFuture<List<ItemStack>> clearInventory(@NotNull OfflinePlayer player) {
    // Connected ?
    if(player.isOnline()) {
      Player onlinePlayer = Bukkit.getPlayer(player.getUniqueId());
      if(onlinePlayer != null) {
        return CompletableFuture.completedFuture(connectedPlayerClear(onlinePlayer));
      }
    }

    // Get User from UUID
    return Main.getToClearPlayersList().markAsClear(player.getUniqueId()).thenCompose(x ->
       HuskSyncAPI.getInstance().getUser(player.getUniqueId())
        .thenApply(optionalUser -> {
              if (optionalUser.isEmpty()) {
                LOG.warn("Internal player not found: {}.", player.getName());
                return null;
              }
              return optionalUser.get();
            })

        // Get data of user
        .thenCompose(user -> {
          if(user == null) return CompletableFuture.completedFuture(List.of());
          return disconnectedPlayerClear(user);
        })
    );
  }

  private static @NotNull List<ItemStack> connectedPlayerClear(@NotNull Player player) {
    List<ItemStack> output = new ArrayList<>();
    Arrays.stream(player.getInventory().getContents()).forEach(item -> {
      if (item != null) output.add(item.clone());
    });

    // On vide via Bukkit. HuskSync sauvegardera cet état vide à sa prochaine déconnexion.
    player.getInventory().clear();
    return output;
  }

  private static @NotNull CompletableFuture<List<ItemStack>> disconnectedPlayerClear(@NotNull User user) {
    CompletableFuture<List<ItemStack>> future = new CompletableFuture<>();
    HuskSyncAPI.getInstance().editCurrentData(user, editor -> {
      // Get the inventory data
      List<ItemStack> output = new ArrayList<>();
      if (editor.getInventory().isEmpty()) {
        future.complete(output);
        return;
      }
      var inventory = editor.getInventory().get();

      // Save items
      if(inventory instanceof BukkitData.Items.Inventory bukkitInventory) {
        Arrays.stream(bukkitInventory.getContents()).forEach(itemStack -> {
          if(itemStack != null)
            output.add(itemStack.clone());
        });
      } else {
        future.completeExceptionally(new RuntimeException("Inventory is not a Bukkit inventory, cannot clear it. Type: '" + inventory.getClass() + "'."));
        return;
      }

      // Clear vanilla inventory
      inventory.clear();

      // Complete promise
      future.complete(output);
    });
    return future;
  }

  /**
   * Get the registered server ID.
   * @return a non-null server ID.
   */
  public static @NotNull String getServerId() {
    return HuskSyncAPI.getInstance().getPlugin().getServerName();
  }

}
