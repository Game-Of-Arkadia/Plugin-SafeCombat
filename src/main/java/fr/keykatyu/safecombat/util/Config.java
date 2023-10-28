package fr.keykatyu.safecombat.util;

import fr.keykatyu.safecombat.Main;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CONFIG class utility
 * Variable getters and setters
 * Imported from other projects, by KeyKatyu
 */
public class Config {

    public static String getString(String path) {
        return Objects.requireNonNull(Main.getInstance().getConfig().getString(path)).replaceAll("&", "§");
    }

    public static List<String> getStringList(String path) {
        return Main.getInstance().getConfig().getStringList(path);
    }

    public static Map<String, Object> getMap(String path) {
        return Main.getInstance().getConfig().getConfigurationSection(path).getValues(false);
    }

    public static List<Material> getMaterialList(String path) {
        List<Material> materials = new ArrayList<>();
        for(String str : getStringList(path)) {
            materials.add(Material.matchMaterial(str));
        }
        return materials;
    }

    public static Integer getInt(String path) {
        return Main.getInstance().getConfig().getInt(path);
    }

    public static Boolean getBoolean(String path) {
        return Main.getInstance().getConfig().getBoolean(path);
    }

    public static Double getDouble(String path) {
        return Main.getInstance().getConfig().getDouble(path);
    }

    public static float getFloat(String path) {
        return (float) Main.getInstance().getConfig().getDouble(path);
    }

    public static void setString(String path, String data) {
        Main.getInstance().getConfig().set(path, data);
        Main.getInstance().saveConfig();
    }

    public static void setInt(String path, int data) {
        Main.getInstance().getConfig().set(path, data);
        Main.getInstance().saveConfig();
    }

    public static void setBoolean(String path, boolean data) {
        Main.getInstance().getConfig().set(path, data);
        Main.getInstance().saveConfig();
    }

    public static void setDouble(String path, double data) {
        Main.getInstance().getConfig().set(path, data);
        Main.getInstance().saveConfig();
    }

    public static void setStringList(String path, List<String> list) {
        Main.getInstance().getConfig().set(path, list);
        Main.getInstance().saveConfig();
    }

    public static void setMap(String path, Map<String, Object> map) {
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            Main.getInstance().getConfig().set(path + "." + entry.getKey(), entry.getValue());
        }
        Main.getInstance().saveConfig();
    }

}