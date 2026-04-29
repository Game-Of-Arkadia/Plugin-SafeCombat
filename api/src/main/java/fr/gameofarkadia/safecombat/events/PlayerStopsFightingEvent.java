package fr.gameofarkadia.safecombat.events;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} exits combat.
 */
@Getter
public class PlayerStopsFightingEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final OfflinePlayer player;
    private final Reason reason;

    /**
     * New instance.
     * @param player player to exit fight.
     */
    public PlayerStopsFightingEvent(@NotNull OfflinePlayer player, @NotNull Reason reason) {
        this.player = player;
        this.reason = reason;
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

    public enum Reason {
        AFTER_DURATION,

        DISCONNECT,

        OTHER,
    }

}