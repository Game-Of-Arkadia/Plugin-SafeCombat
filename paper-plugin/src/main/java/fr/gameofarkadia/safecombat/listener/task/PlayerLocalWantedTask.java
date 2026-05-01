package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.combat.FightStopReason;
import fr.gameofarkadia.safecombat.wanted.PunishmentHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The player was here, and disconnected in combat.<br/>
 * The task displays particle at its location.<br/>
 * If reconnects, should disable.
 */
public class PlayerLocalWantedTask {

  private final BukkitTask taskPunish;
  private final BukkitTask taskParticles;
  private final UUID playerUUID;
  private final String playerName;
  private final Location location;

  /**
   * Create, register and start the task.
   * @param player player to start the task for. It will only access to location and not mutate anything directly.
   */
  public PlayerLocalWantedTask(@NotNull Player player) {
    this.playerUUID = player.getUniqueId();
    this.playerName = player.getName();
    location = player.getLocation().clone().add(0, 0.6, 0);

    var duration = Main.config().getPvpConfiguration().getDurationBeforePunishment();
    taskPunish = SafeCombatScheduler.runLaterAsync(this::applyPunishmentWhenNotReconnected, duration.asTicks());
    taskParticles = SafeCombatScheduler.runTimer(this::spawnParticles, 10);
  }

  /// Called once after the punishment delay, if the player did not reconnect.
  private void applyPunishmentWhenNotReconnected() {
    Main.logger().warn("Player {} did not reconnect in time. Will be punished.", playerName);
    cancel();
    Bukkit.broadcast(Component.text(Main.prefix() + "§7Le joueur §4" + playerName + "§7 ne s'est pas reconnecté à temps. Il a été puni."));

    // Clear fight status
    SafeCombatAPI.getCombatManager().clearFightStatus(playerUUID, FightStopReason.AFTER_DURATION);

    // Punish
    PunishmentHelper.applyPunishment(playerUUID, location);

    // Clear status on manager
    SafeCombatAPI.getWantedPlayersManager().clearLocalWanted(playerUUID);
  }

  /// Called every second
  private void spawnParticles() {
    location.getWorld()
        .spawnParticle(Particle.DUST, location, 10, 0.2, 0.5, 0.2, new Particle.DustOptions(Color.RED, 2));
  }

  /**
   * Simply cancel. Can be used when player reconnects.
   */
  public void cancel() {
    taskPunish.cancel();
    taskParticles.cancel();
  }

}