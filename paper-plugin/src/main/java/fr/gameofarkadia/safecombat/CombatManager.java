package fr.gameofarkadia.safecombat;

import fr.gameofarkadia.safecombat.listener.task.PlayerFightingTask;
import fr.gameofarkadia.safecombat.listener.task.PlayerProtectedTask;
import fr.gameofarkadia.safecombat.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages which player is fighting with who.
 */
public class CombatManager {

    private final Map<String, PlayerFightingTask> fightingPlayers = new HashMap<>();
    private final Map<UUID, PlayerProtectedTask> protectedPlayers = new HashMap<>();
    private final List<String> playersToKill;

    public CombatManager(@NotNull List<String> playersToKill, @NotNull Map<String, Object> protectedPlayers) {
        this.playersToKill = playersToKill;
        protectedPlayers.forEach((str, obj) -> {
            if(str.equalsIgnoreCase("uuid")) return;
            UUID uuid = UUID.fromString(str);
            this.protectedPlayers.put(uuid, new PlayerProtectedTask(Bukkit.getOfflinePlayer(uuid), Instant.ofEpochMilli((Long) obj), 1200));
        });
    }

    public Map<String, PlayerFightingTask> getFightingPlayers() {
        return fightingPlayers;
    }

    public List<String> getPlayersToKill() {
        return playersToKill;
    }

    public Map<UUID, PlayerProtectedTask> getProtectedPlayers() {
        return protectedPlayers;
    }

    /**
     * Make the player fighting : run the task and show the messages & titles
     * @param player The player
     */
    public void setPlayerFighting(Player player) {
        fightingPlayers.put(player.getName(), new PlayerFightingTask(player));
        player.sendMessage(Util.prefix() + Main.getLang().get("fight.enter"));
    }

    /**
     * Make the player protected - it can't pvp and be attacked
     * @param player The player
     * @param protectionEnd The Instant the protection ends
     * @param taskPeriod Task period
     */
    public void setPlayerProtected(OfflinePlayer player, Instant protectionEnd, long taskPeriod) {
        protectedPlayers.put(player.getUniqueId(), new PlayerProtectedTask(player, protectionEnd, taskPeriod));
    }

    /**
     * Check if player is in fight
     * @param player The player
     * @return true or false
     */
    public boolean isFighting(Player player) {
        return fightingPlayers.containsKey(player.getName());
    }

    /**
     * Check if player is protected
     * @param player The player
     * @return true or false
     */
    public boolean isProtected(OfflinePlayer player) {
        return protectedPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Update starting instant (if the player is damaged again)
     * @param player The player
     */
    public void updateInstant(Player player) {
        fightingPlayers.get(player.getName()).setStartingInstant(Instant.now());
    }

}