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

    public static <T> void setStringList(String path, Collection<T> list) {
        Main.getInstance().getConfig().set(path, list.stream().map(String::valueOf).toList());
        Main.getInstance().saveConfig();
    }

    public static void setMap(String path, Map<String, Object> map) {
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            Main.getInstance().getConfig().set(path + "." + entry.getKey(), entry.getValue());
        }
        Main.getInstance().saveConfig();
    }

}