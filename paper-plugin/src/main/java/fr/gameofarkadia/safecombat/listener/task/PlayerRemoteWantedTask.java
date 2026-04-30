package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.wanted.WantedPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * This task will try to search for a connected player. If he exists, transfer him to the wanted server.
 */
public class PlayerRemoteWantedTask {

  private final BukkitTask task;
  private final WantedPlayer data;

  /**
   * Create, register and start the task.
   * @param data data to look for.
   */
  public PlayerRemoteWantedTask(@NotNull WantedPlayer data) {
    this.data = data;
    task = SafeCombatScheduler.runTimerAsync(this::tickSearchPlayer, 30L);
  }

  private void tickSearchPlayer() {
    Player player = Bukkit.getPlayer(data.uuid());
    if (player != null) {
      // Player found, transfer him to wanted server
      Main.logger().info("Found player {}. Will try to transfert it to {}.", player.getName(), data.uuid());
      player.sendMessage(Main.prefix() + "§cTu as quitté un serveur en combat ! Transfert en cours...");
      Main.playerTransfertHandler().transferPlayer(player, data.serverId());

      // Do nothing with status. It's the RPC job to clear wanted-status.
      // This server with just stop looking for the player.
      cancel();
    }
  }

  /**
   * Cancel the task.
   */
  public void cancel() {
    task.cancel();
  }

}