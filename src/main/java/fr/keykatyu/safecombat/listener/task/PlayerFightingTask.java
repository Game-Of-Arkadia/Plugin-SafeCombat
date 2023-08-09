package fr.keykatyu.safecombat.listener.task;

import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.util.Config;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;

public class PlayerFightingTask implements Runnable {

    private final int taskId;
    private Instant startingInstant;
    private final Player player;
    private final BossBar bossBar;

    public PlayerFightingTask(Player player) {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this, 0, 20).getTaskId();
        startingInstant = Instant.now();
        this.player = player;
        bossBar = Bukkit.createBossBar("§4§l⚔ COMBAT §8| §c§l" + Config.getInt("pvp.duration") + "§cs restantes", BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
    }

    @Override
    public void run() {
        Duration duration = Duration.between(startingInstant, Instant.now());
        if(duration.toSeconds() >= Config.getInt("pvp.duration")) {
            cancel();
            bossBar.removeAll();
            bossBar.setVisible(false);
            Main.getCombatManager().getFightingPlayers().remove(player.getName());
            player.sendMessage(Util.prefix() + Config.getString("messages.fight.finished"));
        } else {
            int timeLeft = (int) (Config.getInt("pvp.duration") - duration.toSeconds());
            bossBar.setTitle("§4§l⚔ COMBAT §8| §c§l" + timeLeft + "§cs restantes");
            bossBar.setProgress((double) timeLeft / Config.getInt("pvp.duration"));
        }
    }

    private void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public void setStartingInstant(Instant startingInstant) {
        this.startingInstant = startingInstant;
    }

}