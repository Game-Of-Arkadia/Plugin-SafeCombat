package fr.gameofarkadia.safecombat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} exits combat.
 */
public class PlayerStopsFightingEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;

    /**
     * New instance.
     * @param player player to exit fight.
     */
    public PlayerStopsFightingEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Get the player concerned
     * @return The player
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Bukkit boilerplate.
     * @return handlers.
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}