package fr.gameofarkadia.safecombat.listener.task;

import com.google.common.base.Preconditions;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.protection.ProtectedData;
import fr.gameofarkadia.safecombat.util.Util;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Displays a boss-bar to p^rotected player.<br/>
 * If a protection ends, this class will handle its removal.
 */
public class PlayerProtectedTask implements Runnable {

  private final int taskId;
  private final ProtectedData protection;
  private final OfflinePlayer offlinePlayer;
  private BossBar bossBar;
  private final Duration totalDuration;

  /**
   * Create a start a task.

   * @param data protection data.
   */
  public PlayerProtectedTask(@NotNull ProtectedData data) {
    taskId = SafeCombatScheduler.runTimerAsync(this, 20).getTaskId();
    this.protection = data;
    this.offlinePlayer = Bukkit.getOfflinePlayer(data.player());

    totalDuration = Duration.between(protection.protectionStarted(), protection.protectionFinish());
  }

  private @NotNull BossBar getBossBar() {
    if(bossBar == null) {
      bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_20);
      ifOnline(bossBar::addPlayer);
      bossBar.setVisible(true);
    }
    return bossBar;
  }

  @Override
  public void run() {
    // Check if over
    if (protection.isOver()) {
      SafeCombatAPI.getProtectionManager().removePlayerProtection(offlinePlayer);
      ifOnline(p -> p.sendMessage(Util.prefix() + Main.getLang().get("protection.finished")));
      cancel();
      return;
    }

    if(isOffline()) return;

    // Update boss-bar
    Duration duration = protection.duration();
    getBossBar().setTitle(Main.getLang().get("protection.boss-bar")
        .replaceAll("%duration%", DurationFormatUtils.formatDuration(duration.toMillis(), Main.getLang().get("protection.duration-format"), false)));
    getBossBar().setProgress((double) duration.toMillis() / totalDuration.toMillis());
  }

  /**
   * Update the online player.
   * @param player player.
   */
  public void updatePlayer(@NotNull Player player) {
    Preconditions.checkArgument(player.getUniqueId().equals(offlinePlayer.getUniqueId()), "Player must be the same as the one of this task.");
    bossBar.removeAll();
    bossBar.addPlayer(player);
  }

  private boolean isOffline() {
    return ! offlinePlayer.isOnline();
  }

  private void ifOnline(Consumer<Player> action) {
    Player player = offlinePlayer.getPlayer();
    if (player != null)
      action.accept(player);
  }

  /**
   * Cancel this task.
   */
  public void cancel() {
    Bukkit.getScheduler().cancelTask(taskId);
    bossBar.removeAll();
    bossBar.setVisible(false);
  }

}