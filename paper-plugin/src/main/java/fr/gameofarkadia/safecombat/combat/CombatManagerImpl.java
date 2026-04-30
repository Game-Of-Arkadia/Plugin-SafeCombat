package fr.gameofarkadia.safecombat.combat;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.events.PlayerStartsFightingEvent;
import fr.gameofarkadia.safecombat.events.PlayerStopsFightingEvent;
import fr.gameofarkadia.safecombat.listener.task.PlayerFightingTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
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
    Bukkit.getPluginManager().callEvent(
        new PlayerStartsFightingEvent(player, isAttacked ? PlayerStartsFightingEvent.Type.ATTACKED : PlayerStartsFightingEvent.Type.ATTACKER)
    );
  }

  @Override
  public void clearFightStatus(@NotNull UUID uuid, @NotNull FightStopReason reason) {
    var task = localFightingPlayers.remove(uuid);
    if(task != null) {
      task.cancel(false);
      Player player = Bukkit.getPlayer(uuid);
      if(player != null) {
        Bukkit.getPluginManager().callEvent(new PlayerStopsFightingEvent(player, reason));
      }
    }
  }
}
