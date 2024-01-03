/*
 * Copyright (C) 2024. KeyKatyu / Antoine D. (keykatyu@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package fr.keykatyu.safecombat.util;

import fr.keykatyu.safecombat.Main;
import org.bukkit.Material;

import java.util.*;

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
        if(Main.getInstance().getConfig().getConfigurationSection(path) != null) {
            return Main.getInstance().getConfig().getConfigurationSection(path).getValues(false);
        }
        return new HashMap<>();
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