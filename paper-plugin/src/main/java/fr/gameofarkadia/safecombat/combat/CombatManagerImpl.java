package fr.gameofarkadia.safecombat.combat;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import fr.gameofarkadia.safecombat.events.PlayerStartsFightingEvent;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import fr.gameofarkadia.safecombat.listener.task.PlayerFightingTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Local combat management is transient.
 */
public class CombatManagerImpl implements CombatManager {

  private final Map<UUID, PlayerFightingTask> localFightingPlayers = new HashMap<>();

  @Override
  public boolean isFighting(@NotNull UUID playerUUID) {
    return localFightingPlayers.containsKey(playerUUID);
  }

  @Override
  public void setStartedFight(@NotNull Player player, boolean isAttacked) {
    UUID uuid = player.getUniqueId();
    if(isFighting(uuid)) {
      localFightingPlayers.get(uuid).refresh();
      return;
    }

    // Register + message
    localFightingPlayers.put(uuid, new PlayerFightingTask(player));
    player.sendMessage(Main.prefix() + "§cVous êtes entré en combat. Ne vous déconnectez pas !");
    player.playSound(player, Sound.ENTITY_CREAKING_ACTIVATE, 1f, 0.85f);

    // Propagate event
    SafeCombatScheduler.event(
        new PlayerStartsFightingEvent(player, isAttacked ? PlayerStartsFightingEvent.Type.ATTACKED : PlayerStartsFightingEvent.Type.ATTACKER)
    );
  }

  @Override
  public void refreshFight(@NotNull Player player) {
    var task = localFightingPlayers.get(player.getUniqueId());
    if(task != null) task.refresh();
  }

  @Override
  public void clearFightStatus(@NotNull UUID uuid, @NotNull FightStopReason reason) {
    Main.logger().warn("[DEBUG] Clearing fight status of player {} for reason {}", uuid, reason);
    var task = localFightingPlayers.remove(uuid);
    if(task != null) {
      task.cancel(false);
      Player player = Bukkit.getPlayer(uuid);
      if(player != null) {
        SafeCombatScheduler.event(new PlayerStopsFightingEvent(player, reason));
      }
    }
  }

  @Override
  public void signalPlayerReconnect(@NotNull Player player) {
    Optional.ofNullable(localFightingPlayers.get(player.getUniqueId()))
        .ifPresent(task -> task.reconnect(player));
  }
}
