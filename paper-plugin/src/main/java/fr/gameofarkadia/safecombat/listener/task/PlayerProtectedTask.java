package fr.gameofarkadia.safecombat.listener.task;

import com.google.common.base.Preconditions;
import fr.gameofarkadia.arkadialib.api.utils.DurationUtils;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.protection.ProtectedData;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Displays a boss-bar to p^rotected player.<br/>
 * If a protection ends, this class will handle its removal.
 */
public class PlayerProtectedTask implements Runnable {

  private static final String SHIELD = "\uD83D\uDEE1";

  private final BukkitTask task;
  private final ProtectedData protection;
  private final UUID uuid;
  private BossBar bossBar;
  private final Duration totalDuration;

  /**
   * Create a start a task.

   * @param data protection data.
   */
  public PlayerProtectedTask(@NotNull ProtectedData data) {
    this.task = SafeCombatScheduler.runTimerAsync(this, 20);
    this.protection = data;
    this.uuid = data.player();

    totalDuration = Duration.between(protection.protectionStarted(), protection.protectionFinish());
  }

  private @NotNull BossBar getBossBar() {
    if(bossBar == null) {
      bossBar = Bukkit.createBossBar("§b<protection>", BarColor.BLUE, BarStyle.SEGMENTED_20);
      ifOnline(bossBar::addPlayer);
      bossBar.setVisible(true);
    }
    return bossBar;
  }

  @Override
  public void run() {
    // Check if over
    if (protection.isOver()) {
      Main.logger().info("Player protection ended: {} by task.", protection);
      SafeCombatAPI.getProtectionManager().removePlayerProtection(uuid);
      ifOnline(p -> p.sendMessage(Main.prefix() + "§eVotre protection a pris fin. Vous pouvez désormais§c attaquer§e et§c être attaqué§e par d'autres joueurs."));
      cancel();
      return;
    }

    if(isOffline()) return;

    // Update boss-bar
    Duration remaining = protection.remaining();
    getBossBar().setTitle(("§9§l"+SHIELD+" §b§lProtection §7|§b Reste §6{duration} §9§l" + SHIELD)
        .replace("{duration}", DurationUtils.formatDuration(remaining)));
    getBossBar().setProgress((double) remaining.toMillis() / totalDuration.toMillis());
  }

  /**
   * Update the online player.
   * @param player player.
   */
  public void updatePlayer(@NotNull Player player) {
    Preconditions.checkArgument(uuid.equals(player.getUniqueId()), "Player must be the same as the one of this task.");
    bossBar.removeAll();
    bossBar.addPlayer(player);
  }

  private boolean isOffline() {
    return Bukkit.getPlayer(uuid) == null;
  }

  private void ifOnline(@NotNull Consumer<Player> action) {
    Player player = Bukkit.getPlayer(uuid);
    if (player != null)
      action.accept(player);
  }

  /**
   * Cancel this task.
   */
  public void cancel() {
    task.cancel();
    if(bossBar != null) {
      bossBar.removeAll();
      bossBar.setVisible(false);
    }
  }

}