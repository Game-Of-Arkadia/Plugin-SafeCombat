package fr.keykatyu.safecombat;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
    }

    public static Main getInstance() {
        return INSTANCE;
    }

}