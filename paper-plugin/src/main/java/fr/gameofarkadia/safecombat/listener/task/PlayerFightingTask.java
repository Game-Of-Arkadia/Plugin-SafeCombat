package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import fr.gameofarkadia.safecombat.util.Config;
import fr.gameofarkadia.safecombat.util.Util;
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
    private final Player player;
    private final BossBar bossBar;

    public PlayerFightingTask(@NotNull Player player) {
        taskId = SafeCombatScheduler.runTimerAsync(this, 20).getTaskId();
        startingInstant = Instant.now();
        this.player = player;

        bossBar = Bukkit.createBossBar(Main.getLang().get("fight.boss-bar").replaceAll("%duration%", String.valueOf(Config.getInt("pvp.duration"))), BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
    }

    @Override
    public void run() {
        Duration duration = Duration.between(startingInstant, Instant.now());
        if(duration.toSeconds() >= Config.getInt("pvp.duration")) {
            cancel(true);
        } else {
            int timeLeft = (int) (Config.getInt("pvp.duration") - duration.toSeconds());
            bossBar.setTitle(Main.getLang().get("fight.boss-bar").replaceAll("%duration%", String.valueOf(timeLeft)));
            bossBar.setProgress((double) timeLeft / Config.getInt("pvp.duration"));
        }
    }

    public void cancel(boolean propagate) {
        // remove task and boss-bar
        Bukkit.getScheduler().cancelTask(taskId);
        bossBar.removeAll();
        bossBar.setVisible(false);

        // Call event, propagate... if not wanted
        if(propagate && ! SafeCombatAPI.isWanted(player)) {
            SafeCombatScheduler.run(() -> Bukkit.getPluginManager().callEvent(new PlayerStopsFightingEvent(player, PlayerStopsFightingEvent.Reason.AFTER_DURATION)));
            SafeCombatAPI.getCombatManager().clearFightStatus(player);
            player.sendMessage(Util.prefix() + Main.getLang().get("fight.finished"));
        }
    }

    /**
     * Refresh the fighting-state.
     */
    public void refresh() {
        startingInstant = Instant.now();
    }

}