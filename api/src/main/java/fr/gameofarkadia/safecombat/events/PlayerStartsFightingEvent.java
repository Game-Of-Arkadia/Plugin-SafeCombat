package fr.gameofarkadia.safecombat.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} enters combat.
 */
@Getter
public class PlayerStartsFightingEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Type type;

    /**
     * New event instance.
     * @param player the player.
     * @param type either "attacker" or "attacked".
     */
    public PlayerStartsFightingEvent(@NotNull Player player, @NotNull Type type) {
        this.player = player;
        this.type = type;
    }

    /**
     * Get the player concerned
     * @return The player
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Get the player type : attacked or attacker.
     * @return The event type
     */
    public @NotNull Type getType() {
        return type;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public enum Type {
        ATTACKER,
        ATTACKED
    }

}