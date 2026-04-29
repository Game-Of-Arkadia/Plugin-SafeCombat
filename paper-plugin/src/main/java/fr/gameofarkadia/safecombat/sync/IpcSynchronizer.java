package fr.gameofarkadia.safecombat.sync;

import fr.arkadia.pterodactyl.api.PterodactylAPI;
import fr.arkadia.pterodactyl.api.ipc.IpcChannelListener;
import fr.arkadia.pterodactyl.api.ipc.IpcNotification;
import fr.gameofarkadia.safecombat.Main;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * IPC Synchronizer.
 */
public class IpcSynchronizer implements IpcChannelListener {

  private static final String IPC_CHANNEL = "safe_combat_events";

  /**
   * Register a new synchronizer.
   */
  public IpcSynchronizer() {
    PterodactylAPI.getIpcPublisher()
        .getChannel(IPC_CHANNEL)
        .registerListener(this);
  }

  /**
   * Send an RPC across servers.
   * @param command command to propagate.
   * @param args arguments to add.
   */
  public void sendRpc(@NotNull SyncCommand command, @NotNull Object @NotNull ... args) {
    command.checkValid(args);

    // Actually send
    PterodactylAPI.getIpcPublisher()
        .getChannel(IPC_CHANNEL)
        .sendNotification(new CombatRpc(command, args));
  }

  @Override
  public void receiveNotification(@NotNull IpcNotification ipcNotification) {
    if(!(ipcNotification instanceof CombatRpc rpc)) {
      Main.logger().warn("Received unknown notification type: '{}'.", ipcNotification.getClass().getSimpleName());
      return;
    }

    switch (rpc.command()) {
      case SyncCommand.PLAYER_DISCONNECTED -> {
        UUID playerUUID = rpc.get(0, UUID.class);
        String serverId = rpc.get(1, String.class);
        long disconnectTs = rpc.get(2, Long.class);

        Main.logger().info("Server {} sent a PLAYER_DISCONNECTED for {}. TS={}.", serverId, playerUUID, disconnectTs);
      }
      default -> Main.logger().error("Received unknown sync command: '{}'.", rpc.command());
    }
  }
}
