package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.arkadialib.api.utils.DurationWrapper;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class PlayerDisconnectedTask implements Runnable {

    private final int taskId;
    private final int particlesTasks;
    private final OfflinePlayer player;
    private final Location location;
    private final ItemStack[] items;

    public PlayerDisconnectedTask(@NotNull Player player, @NotNull DurationWrapper duration) {
        this.player = player;
        location = player.getLocation().clone().add(0, 0.5, 0);
        items = player.getInventory().getContents();
        taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), this, duration.asTicks()).getTaskId();
        particlesTasks = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::spawnParticles, 0, 20).getTaskId();
    }

    @Override
    public void run() {
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