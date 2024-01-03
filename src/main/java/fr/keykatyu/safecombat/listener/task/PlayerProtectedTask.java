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
import fr.keykatyu.safecombat.util.Util;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.time.Duration;
import java.time.Instant;

public class PlayerProtectedTask implements Runnable {

    private final int taskId;
    private final Instant protectionStart;
    private final Instant protectionEnd;
    private final OfflinePlayer offlinePlayer;
    private final BossBar bossBar;

    public PlayerProtectedTask(OfflinePlayer offlinePlayer, Instant protectionEnd, long taskPeriod) {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this, 10, taskPeriod).getTaskId();
        protectionStart = Instant.now();
        this.protectionEnd = protectionEnd;
        this.offlinePlayer = offlinePlayer;

        bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_20);
        if(offlinePlayer.isOnline()) bossBar.addPlayer(offlinePlayer.getPlayer());
        bossBar.setVisible(true);
    }

    @Override
    public void run() {
        Duration duration = Duration.between(Instant.now(), protectionEnd);
        if(duration.isNegative() || duration.isZero()) {
            cancel();
            Main.getCombatManager().getProtectedPlayers().remove(offlinePlayer.getUniqueId());
            if(offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(Util.prefix() + Main.getLang().get("protection.finished"));
        } else {
            bossBar.setTitle(Main.getLang().get("protection.boss-bar").replaceAll("%duration%", DurationFormatUtils.formatDuration(duration.toMillis(), Main.getLang().get("protection.duration-format"), false)));
            Duration totalDuration = Duration.between(protectionStart, protectionEnd);
            bossBar.setProgress((double) duration.toMillis() / totalDuration.toMillis());
        }
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
        bossBar.removeAll();
        bossBar.setVisible(false);
    }

    public Instant getProtectionEnd() {
        return protectionEnd;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

}