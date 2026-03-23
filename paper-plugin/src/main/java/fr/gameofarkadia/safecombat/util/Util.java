package fr.gameofarkadia.safecombat.util;

import fr.gameofarkadia.safecombat.Main;
import org.bukkit.Bukkit;

/**
 * Utils
 */
public final class Util {
    private Util() {}

    /**
     * Get the prefix for the chat messages
     * @return The prefix
     */
    public static String prefix() {
        return Main.getLang().get("prefix") + " ";
    }

    /**
     * Send a console message with the plugin prefix
     * @param msg The message to send
     */
    public static void console(String msg) {
        Bukkit.getConsoleSender().sendMessage(prefix() + msg);
    }

}