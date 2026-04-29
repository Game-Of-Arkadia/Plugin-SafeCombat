package fr.gameofarkadia.safecombat.listener.task;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import fr.gameofarkadia.safecombat.wanted.WantedPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The player was here, and disconnected in combat.<br/>
 * The task displays particle at its location.<br/>
 * If reconnects, should disable.
 */
public class PlayerLocalWantedTask {

  private final BukkitTask taskPunish;
  private final BukkitTask taskParticles;
  private final OfflinePlayer player;
  private final Location location;

  public PlayerLocalWantedTask(@NotNull Player player) {
    this.player = player;
    location = player.getLocation().clone().add(0, 0.5, 0);

    var duration = Main.config().getPvpConfiguration().getDurationBeforePunishment();
    taskPunish = SafeCombatScheduler.runLaterAsync(this::tickDespawn, duration.asTicks());
    taskParticles = SafeCombatScheduler.runTimer(this::spawnParticles, 20);
  }

  /// Cal
  private void tickDespawn() {
    Bukkit.broadcast(Component.text("§6§l" + player.getName() + " §c" + Main.getLang().get("fight.player-disconnected-punishment")));
    Main.getCombatManager().addPlayerToKill(player);

    if (Main.getCombatManager().removeFromFighting(player)) {
      Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> Bukkit.getPluginManager().callEvent(new PlayerStopsFightingEvent(player)));
    }

    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
      Main.getInstance().getSLF4JLogger().warn("Player {} will drop inventory", player.getName());
      for (ItemStack itemStack : items) {
        if (itemStack == null) continue;
        location.getWorld().dropItem(location, itemStack);
      }
    });
    cancel();
  }

  /// All every second
  private void spawnParticles() {
    location.getWorld().spawnParticle(Particle.DUST, location, 5, 0.15, 0.4, 0.15, new Particle.DustOptions(Color.RED, 2));
  }

  /**
   * Simply cancel. Can be used when player reconnects.
   */
  public void cancel() {
    taskPunish.cancel();
    taskParticles.cancel();
  }

}