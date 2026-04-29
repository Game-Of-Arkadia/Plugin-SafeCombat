package fr.gameofarkadia.safecombat.wanted;

import com.google.common.base.Preconditions;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.listener.task.PlayerWantedTask;
import fr.gameofarkadia.safecombat.sync.SyncCommand;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WantedPlayersManagerImpl implements WantedPlayersManager {

  private final Map<UUID, PlayerWantedTask> localTasks = new ConcurrentHashMap<>();
  private final Map<UUID, WantedPlayer> wantedPlayers = new ConcurrentHashMap<>();

  @Override
  public boolean isWanted(@NotNull UUID uuid) {
    return wantedPlayers.containsKey(uuid);
  }

  @Override
  public @NotNull WantedPlayer getWanted(@NotNull OfflinePlayer player) throws NoSuchElementException {
    return Optional.ofNullable(wantedPlayers.get(player.getUniqueId())).orElseThrow();
  }

  // -- set

  @Override
  public void setLocalWanted(@NotNull WantedPlayer data) {
    if(isWanted(data.uuid())) {
      Main.logger().warn("Received (local) {} but player is already wanted. Ignoring.", data);
      return;
    }

    wantedPlayers.put(data.uuid(), data);
    localTasks.put(data.uuid(), new PlayerWantedTask(data));
  }

  @Override
  public void setNetworkWanted(@NotNull WantedPlayer data) {
    Preconditions.checkArgument(data.isLocal(), "Cannot propagate " + data + " as it's not from this server.");
    if(isWanted(data.uuid())) {
      Main.logger().warn("Received (network) {} but player is already wanted. Ignoring.", data);
      return;
    }
    // Set local
    setLocalWanted(data);
    // Propagate over network
    Main.synchronizer().sendRpc(SyncCommand.WANTED_NEW, data.uuid(), data.serverId(), data.timestamp());
  }

  // -- clear

  @Override
  public void clearLocalWanted(@NotNull UUID uuid) {
    wantedPlayers.clear();
    var task = localTasks.remove(uuid);
    if(task != null) {
      task.cancel();
    }
  }

  @Override
  public void clearNetworkWanted(@NotNull UUID uuid) {
    clearLocalWanted(uuid);
    // Propagate over network
    Main.synchronizer().sendRpc(SyncCommand.WANTED_CLEAR, uuid);
  }

}
