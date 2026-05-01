package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.arkadialib.api.utils.DurationUtils;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.combat.FightStopReason;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

/**
 * Disable PvP automatically after duration.
 */
public class PlayerFightingTask implements Runnable {

  private final int taskId;
  private Instant startingInstant;
  private final Duration totalDuration;
  private final Player player;
  private final BossBar bossBar;

  public PlayerFightingTask(@NotNull Player player) {
    this.player = player;

    startingInstant = Instant.now();
    totalDuration = Main.config().getPvpConfiguration().getFightDuration().asJavaDuration();

    bossBar = Bukkit.createBossBar("§4§l⚔ COMBAT", BarColor.RED, BarStyle.SEGMENTED_10);
    bossBar.addPlayer(player);
    bossBar.setVisible(true);

    taskId = SafeCombatScheduler.runTimerAsync(this, 20).getTaskId();
  }

  @Override
  public void run() {
    Duration elapsed = Duration.between(startingInstant, Instant.now());
    Duration remaining = totalDuration.minus(elapsed);
    if (remaining.isNegative() || remaining.isZero()) {
      cancel(true);
    } else {
      String title = "§4§l⚔ COMBAT §8|§c§l {duration}§c restantes".replace("{duration}", DurationUtils.formatDuration(remaining));
      bossBar.setTitle(title);
      bossBar.setProgress((double) remaining.toMillis() / totalDuration.toMillis());
    }
  }

  public void reconnect(@NotNull Player player) {
    bossBar.removeAll();
    bossBar.addPlayer(player);
  }

  public void cancel(boolean propagate) {
    // remove task and boss-bar
    Bukkit.getScheduler().cancelTask(taskId);
    bossBar.removeAll();
    bossBar.setVisible(false);

    // Call event, propagate... if not wanted
    if (propagate && !SafeCombatAPI.isWanted(player)) {
      SafeCombatAPI.getCombatManager().clearFightStatus(player, FightStopReason.AFTER_DURATION);
      player.sendMessage(Main.prefix() + "§aVous n'êtes plus en combat et pouvez à nouveau vous déconnecter.");
    }
  }

  /**
   * Refresh the fighting-state.
   */
  public void refresh() {
    startingInstant = Instant.now();
  }

}