package fr.gameofarkadia.safecombat.sync;

import com.google.common.base.Preconditions;
import fr.arkadia.pterodactyl.api.PterodactylAPI;
import fr.arkadia.pterodactyl.api.ipc.IpcChannelListener;
import fr.arkadia.pterodactyl.api.ipc.IpcNotification;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.wanted.PunishmentHelper;
import fr.gameofarkadia.safecombat.wanted.WantedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

/**
 * IPC Synchronizer.
 */
public class IpcSynchronizer implements IpcChannelListener {

  private static final String IPC_CHANNEL = "safe_combat_events";
  private boolean initialized;

  public synchronized void initialize() {
    Preconditions.checkState(!initialized, "Already initialized.");
    PterodactylAPI.getIpcPublisher()
        .getChannel(IPC_CHANNEL)
        .registerListener(this);
    initialized= true;
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

    Main.logger().info("[>] Propagated {} with {}.", command, Arrays.toString(args));
  }

  @Override
  public void receiveNotification(@NotNull IpcNotification ipcNotification) {
    if(!(ipcNotification instanceof CombatRpc rpc)) {
      Main.logger().warn("Received unknown notification type: '{}'.", ipcNotification.getClass().getSimpleName());
      return;
    }

    switch (rpc.command()) {
      case SyncCommand.WANTED_NEW -> {
        UUID playerUUID = rpc.get(0, UUID.class);
        String serverId = rpc.get(1, String.class);
        long disconnectTs = rpc.get(2, Long.class);
        WantedPlayer data = new WantedPlayer(playerUUID, serverId, disconnectTs);
        Main.logger().info("[<] Received WANTED_NEW: {}.", data);

        SafeCombatAPI.getWantedPlayersManager().receivedRemoveWanted(data);
      }

      case WANTED_CLEAR -> {
        UUID playerUUID = rpc.get(0, UUID.class);
        Main.logger().info("[<] Received WANTED_CLEAR: {}.", playerUUID);
        SafeCombatAPI.getWantedPlayersManager().clearRemoteWanted(playerUUID);
      }

      case REMOVED_PROTECTION -> {
        UUID playerUUID = rpc.get(0, UUID.class);
        Main.logger().info("[<] Received REMOVED_PROTECTION: {}.", playerUUID);
        SafeCombatAPI.getProtectionManager().playerProtectionRemovedByRemote(playerUUID);
      }

      case BAN_PLAYER -> {
        UUID playerUUID = rpc.get(0, UUID.class);
        String serverFrom = rpc.get(1, String.class);
        Main.logger().info("[<] Received BAN_PLAYER: {} from {}.", playerUUID, serverFrom);
        PunishmentHelper.notifyBanFromServer(playerUUID, serverFrom);
      }

      default -> Main.logger().error("Received unknown sync command: '{}'.", rpc.command());
    }
  }
}
