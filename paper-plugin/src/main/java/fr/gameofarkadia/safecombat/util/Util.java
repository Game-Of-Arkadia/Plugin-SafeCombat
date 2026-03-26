package fr.gameofarkadia.safecombat.util;

import fr.gameofarkadia.safecombat.Main;
import org.jetbrains.annotations.NotNull;

/**
 * Utils
 */
public final class Util {
    private Util() {}

    /**
     * Get the prefix for the chat messages
     * @return The prefix
     */
    public static @NotNull String prefix() {
        return Main.getLang().get("prefix") + " ";
    }

}