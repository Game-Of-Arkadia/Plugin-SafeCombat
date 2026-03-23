package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.util.Util;
import lombok.Getter;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

public class PlayerProtectedTask implements Runnable {

    private final int taskId;
    private final Instant protectionStart;
    @Getter private final Instant protectionEnd;
    private final OfflinePlayer offlinePlayer;
    @Getter private final BossBar bossBar;

    public PlayerProtectedTask(OfflinePlayer offlinePlayer, Instant protectionEnd) {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this, 10, 40).getTaskId();
        protectionStart = Instant.now();
        this.protectionEnd = protectionEnd;
        this.offlinePlayer = offlinePlayer;

        bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_20);
        ifOnline(bossBar::addPlayer);
        bossBar.setVisible(true);
    }

    @Override
    public void run() {
        Duration duration = Duration.between(Instant.now(), protectionEnd);
        if(duration.isNegative() || duration.isZero()) {
            cancel();
            Main.getCombatManager().removePlayerProtection(offlinePlayer);
            ifOnline(p -> p.sendMessage(Util.prefix() + Main.getLang().get("protection.finished")));
        } else {
            bossBar.setTitle(Main.getLang().get("protection.boss-bar").replaceAll("%duration%", DurationFormatUtils.formatDuration(duration.toMillis(), Main.getLang().get("protection.duration-format"), false)));
            Duration totalDuration = Duration.between(protectionStart, protectionEnd);
            bossBar.setProgress((double) duration.toMillis() / totalDuration.toMillis());
        }
    }

    private void ifOnline(Consumer<Player> action) {
        Player player = offlinePlayer.getPlayer();
        if(player != null)
            action.accept(player);
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
        bossBar.removeAll();
        bossBar.setVisible(false);
    }

}