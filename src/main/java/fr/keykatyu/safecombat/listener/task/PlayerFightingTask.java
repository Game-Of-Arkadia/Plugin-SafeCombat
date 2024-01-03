/*
 * Copyright (C) 2024. KeyKatyu / Antoine D. (keykatyu@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        bossBar = Bukkit.createBossBar(Main.getLang().get("fight.boss-bar").replaceAll("%duration%", String.valueOf(Config.getInt("pvp.duration"))), BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
    }

    @Override
    public void run() {
        Duration duration = Duration.between(startingInstant, Instant.now());
        if(duration.toSeconds() >= Config.getInt("pvp.duration")) {
            cancel();
        } else {
            int timeLeft = (int) (Config.getInt("pvp.duration") - duration.toSeconds());
            bossBar.setTitle(Main.getLang().get("fight.boss-bar").replaceAll("%duration%", String.valueOf(timeLeft)));
            bossBar.setProgress((double) timeLeft / Config.getInt("pvp.duration"));
        }
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
        bossBar.removeAll();
        bossBar.setVisible(false);
        Main.getCombatManager().getFightingPlayers().remove(player.getName());
        player.sendMessage(Util.prefix() + Main.getLang().get("fight.finished"));
    }

    public void setStartingInstant(Instant startingInstant) {
        this.startingInstant = startingInstant;
    }

}