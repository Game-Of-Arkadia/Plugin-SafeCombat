package fr.gameofarkadia.safecombat.util;

import fr.gameofarkadia.safecombat.Main;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * CONFIG class utility
 * Variable getters and setters
 * Imported from other projects, by KeyKatyu
 */
public class Config {

    public static @NotNull String getString(@NotNull String path) {
        return Objects.requireNonNull(Main.getInstance().getConfig().getString(path)).replaceAll("&", "§");
    }

    public static List<String> getStringList(@NotNull String path) {
        return Main.getInstance().getConfig().getStringList(path);
    }

    public static Material getMaterial(@NotNull String path) {
        return Material.getMaterial(getString(path).toUpperCase());
    }

    public static Integer getInt(String path) {
        return Main.getInstance().getConfig().getInt(path);
    }

    public static Boolean getBoolean(String path) {
        return Main.getInstance().getConfig().getBoolean(path);
    }

}