package fr.keykatyu.safecombat.listener.task;

import fr.keykatyu.safecombat.Main;
import fr.keykatyu.safecombat.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDisconnectedTask implements Runnable, Listener {

    private final int taskId;
    private final Player player;
    private final Location location;
    private final ItemStack[] items;

    public PlayerDisconnectedTask(Player player) {
        this.player = player;
        location = player.getLocation();
        items = player.getInventory().getContents();
        taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), this, Config.getInt("pvp.disconnection") * 20).getTaskId();
    }

    /**
     * Cancel the task if the player rejoins
     * @param e The event
     */
    @EventHandler
    public void onPlayerJoins(PlayerJoinEvent e) {
        if(!e.getPlayer().getName().equals(player.getName())) return;
        cancel();
    }

    @Override
    public void run() {
        player.getServer().broadcastMessage("§6§l" + player.getName() + " §cs'est déconnecté en combat.");
        Main.getCombatManager().getPlayersToKill().add(player.getName());
        Main.getCombatManager().getFightingPlayers().get(player.getName()).cancel();
        Main.getCombatManager().getFightingPlayers().remove(player.getName());

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            for(ItemStack itemStack : items) {
                if(itemStack == null) continue;
                player.getWorld().dropItem(location, itemStack);
            }
        });
        cancel();
    }

    private void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
        HandlerList.unregisterAll(this);
    }

}