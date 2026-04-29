package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.arkadialib.api.utils.DurationWrapper;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import fr.gameofarkadia.safecombat.wanted.WantedPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Task that :
 * 1. Try to find the wanted player.
 */
public class PlayerWantedTask {

    private final int taskId;
    private final int particlesTasks;
    private final WantedPlayer data;
    private final @Nullable Location location;

    public PlayerWantedTask(@NotNull WantedPlayer data) {
        this.data = data;
        this.location = null;

        var duration = Main.config().getPvpConfiguration().getDurationBeforePunishment();
        taskId = SafeCombatScheduler.runTimerAsync(this::tickDespawn, duration.asTicks()).getTaskId();
        particlesTasks = -1;
    }

    public PlayerWantedTask(@NotNull Player player) {
        this.data = WantedPlayer.of(player.getUniqueId());
        location = player.getLocation().clone().add(0, 0.5, 0);

        var duration = Main.config().getPvpConfiguration().getDurationBeforePunishment();
        taskId = SafeCombatScheduler.runTimerAsync(this::tickDespawn, duration.asTicks()).getTaskId();
        particlesTasks = SafeCombatScheduler.runTimer(this::spawnParticles, 20).getTaskId();
    }

    private void tickDespawn() {
        Bukkit.broadcast(Component.text("§6§l" + player.getName() + " §c" + Main.getLang().get("fight.player-disconnected-punishment")));
        Main.getCombatManager().addPlayerToKill(player);

        if(Main.getCombatManager().removeFromFighting(player)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> Bukkit.getPluginManager().callEvent(new PlayerStopsFightingEvent(player)));
        }

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Main.getInstance().getSLF4JLogger().warn("Player {} will drop inventory", player.getName());
            for(ItemStack itemStack : items) {
                if(itemStack == null) continue;
                location.getWorld().dropItem(location, itemStack);
            }
        });
        cancel();
    }

    private void spawnParticles() {
        location.getWorld().spawnParticle(Particle.DUST, location, 5, 0.15, 0.4, 0.15, new Particle.DustOptions(Color.RED, 2));
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
        Bukkit.getScheduler().cancelTask(particlesTasks);
    }

}