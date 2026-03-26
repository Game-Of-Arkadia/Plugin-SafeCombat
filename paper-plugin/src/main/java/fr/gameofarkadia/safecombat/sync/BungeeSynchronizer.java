package fr.gameofarkadia.safecombat.sync;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.gameofarkadia.safecombat.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Singleton to sync servers instances.
 */
public class BungeeSynchronizer implements PluginMessageListener {

  private static final String CHANNEL_NAME = "BungeeCord";

  private final Logger logger = LoggerFactory.getLogger(BungeeSynchronizer.class.getSimpleName());
  private final @NotNull JavaPlugin plugin;

  private String serverId;
  private CompletableFuture<String> serverIdFuture;

  /**
   * New instance. Register a plugin-messages listener.
   * @param plugin main plugin.
   */
  public BungeeSynchronizer(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
    Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_NAME, this);
  }

  /**
   * Request a server-ID from the proxy.
   * @param player player to piggyback.
   */
  public void requestServerId(@NotNull Player player) {
    logger.debug("Requesting server-id from proxy. Piggy-backing on player {}.", player.getName());
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("GetServer");
    player.sendPluginMessage(plugin, CHANNEL_NAME, out.toByteArray());
  }

  /**
   * Get the server ID, wrapped in a future.
   * @return a future, completing either with a <b>valid</b> server6ID, or failing.
   */
  public synchronized @NotNull CompletableFuture<String> getServerId() {
    if(serverId != null) return CompletableFuture.completedFuture(serverId);
    if(serverIdFuture != null) return serverIdFuture;

    var players = Bukkit.getOnlinePlayers();
    if(players.isEmpty()) return CompletableFuture.failedFuture(new IllegalStateException("No players online to piggy-back the server-ID request on."));

    serverIdFuture = new CompletableFuture<>();
    serverIdFuture.orTimeout(5, java.util.concurrent.TimeUnit.SECONDS).exceptionally(err -> {
      logger.error("Could not get server-ID from proxy within timeout.", err);
      return null;
    });

    // Request the server-ID
    requestServerId(players.iterator().next());

    return serverIdFuture;
  }

  public void getServerId(@NotNull Consumer<String> action) {
    getServerId().thenAccept(action);
  }

  @Override
  public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
    if(!CHANNEL_NAME.equals(channel)) return;

    ByteArrayDataInput in = ByteStreams.newDataInput(message);
    String subchannel = in.readUTF();

    if("GetServer".equals(subchannel) && serverId == null) {
      serverId = in.readUTF();
      Main.logger().info("Received server-ID: '{}'.", serverId);
      if(serverIdFuture != null)
        serverIdFuture.complete(serverId);
    }
  }

  /**
   * Publish an event.
   * @param event event to propagate to all other servers.
   * @param data data to write. Must match the {@link SyncEvent} parameters of the event.
   */
  public void publish(@NotNull SyncEvent event, @NotNull Object @NotNull ... data) {
    // Check data is valid
    event.checkValid(data);

    // Find a player to piggyback.
    var players = Bukkit.getOnlinePlayers();
    if(players.isEmpty()) {
      logger.warn("No players online to piggy-back the sync-event on. Event {} will not be published.", event);
      return;
    }
    Player player = players.iterator().next();

    // Write
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Forward");
    out.writeUTF("ALL");
    event.write(out, data);

    // Send
    player.sendPluginMessage(plugin, CHANNEL_NAME, out.toByteArray());
  }

}
