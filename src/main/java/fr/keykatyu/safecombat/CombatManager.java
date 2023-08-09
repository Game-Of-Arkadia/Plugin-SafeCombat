package fr.keykatyu.safecombat;

import fr.keykatyu.safecombat.listener.task.PlayerFightingTask;
import fr.keykatyu.safecombat.util.Config;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashMap;

public class CombatManager {

    private final HashMap<String, PlayerFightingTask> fightingPlayers;

    public CombatManager() {
        fightingPlayers = new HashMap<>();
    }

    public HashMap<String, PlayerFightingTask> getFightingPlayers() {
        return fightingPlayers;
    }

    /**
     * Make the player fighting : run the task and show the messages & titles
     * @param player The player
     */
    public void setPlayerFighting(Player player) {
        Main.getCombatManager().getFightingPlayers().put(player.getName(), new PlayerFightingTask(player));
        player.sendMessage(Util.prefix() + Config.getString("messages.fight.enter"));
        player.sendTitle("§4§l⚔", "", 5, 25, 5);
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
     * Update starting instant (if the player is damaged again)
     * @param player The player
     */
    public void updateInstant(Player player) {
        fightingPlayers.get(player.getName()).setStartingInstant(Instant.now());
    }

}