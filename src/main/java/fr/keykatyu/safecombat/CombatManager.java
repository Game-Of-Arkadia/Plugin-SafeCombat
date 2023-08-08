package fr.keykatyu.safecombat;

import fr.keykatyu.safecombat.listener.task.PlayerFightingTask;
import fr.keykatyu.safecombat.util.Config;
import fr.keykatyu.safecombat.util.Util;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CombatManager {

    private final List<String> fightingPlayers;

    public CombatManager() {
        fightingPlayers = new ArrayList<>();
    }

    public List<String> getFightingPlayers() {
        return fightingPlayers;
    }

    /**
     * Make the player fighting : run the task and show the messages & titles
     * @param player The player
     */
    public void setPlayerFighting(Player player) {
        Main.getCombatManager().getFightingPlayers().add(player.getName());
        new PlayerFightingTask(player);
        player.sendMessage(Util.prefix() + Config.getString("messages.fight.enter"));
        player.sendTitle("§4§l⚔", "", 5, 25, 5);
    }

    /**
     * Check if player is in fight
     * @param player The player
     * @return true or false
     */
    public boolean isFighting(Player player) {
        return getFightingPlayers().contains(player.getName());
    }

}