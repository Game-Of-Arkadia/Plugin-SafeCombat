package fr.gameofarkadia.safecombat.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.bridge.HuskSyncHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Handles player transfers between servers using BungeeCord plugin messaging.<br/>
 * This channel works because Velocity listen to BungeeCord messages for player transfers by default.
 */
public final class PlayerTransfertHandler {

  private static final String CHANNEL_NAME = "BungeeCord";

  private final JavaPlugin plugin;

  /**
   * Instantiate a new handler, and register plugin-messaging channels.
   * @param plugin plugin to use.
   */
  public PlayerTransfertHandler(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
    Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_NAME);
  }

  /**
   * Unregister plugin-messaging channels.
   */
  public void unregister() {
    Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL_NAME);
  }

  /**
   * Transfer a player to another server via BungeeCord plugin messaging channel.
   * @param player the player to transfer
   * @param serverName the name of the target server
   */
  public void transferPlayer(@NotNull Player player, @NotNull String serverName) {
    HuskSyncHelper.savePlayerInventory(player);

    // Message bytes
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Connect");
    out.writeUTF(serverName);

    // Send transfer message
    player.sendPluginMessage(plugin, CHANNEL_NAME, out.toByteArray());

    // Log
    Main.logger().info("Transfer request sent for {} to {}.", player.getName(), serverName);
  }

}
