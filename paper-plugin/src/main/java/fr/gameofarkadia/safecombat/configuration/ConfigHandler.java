package fr.gameofarkadia.safecombat.configuration;

import fr.gameofarkadia.safecombat.Main;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Abstract class to handle external configuration with default values.
 */
public abstract class ConfigHandler {

    /** INTERNAL folder inside the jar. */
    private static final String DEFAULT_FOLDER = "default_configuration";

    private final File file;
    private MemoryConfiguration data = new MemoryConfiguration();

    /**
     * New instance. Create file from default, if it does not exist.
     * @param pluginFolder data-directory of the plugin.
     * @param fileName name of the file.
     */
    protected ConfigHandler(@NotNull File pluginFolder, String fileName) {
        file = new File(pluginFolder, fileName);
        assertFileExists();
    }

    /**
     * Reload the configuration.
     */
    public final void reload() {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        // Load config in memory.
        data = configuration;
        // Propagate if listen
        configReloaded(configuration);
    }

    /**
     * Callback for when the configuration is reloaded.
     * @param config the loaded configuration.
     */
    protected void configReloaded(@NotNull YamlConfiguration config) {
        // Nothing.
    }

    /**
     * Get the in memory data.
     * @return the data holder.
     */
    protected @NotNull MemoryConfiguration getData() {
        return data;
    }

    private void assertFileExists() {
        if(file.exists()) return;
        // Read data from jar.
        String data = readDefaultConfig();
        // Write it to disk.
        if(data != null)
            writeToDisk(data);
    }

    private @Nullable String readDefaultConfig() {
        String path = DEFAULT_FOLDER + "/" + file.getName();
        try(InputStream is = Main.getInstance().getResource(path)) {
            Objects.requireNonNull(is, "Could not find resource '" + path + "' in jar.");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Main.getInstance().getSLF4JLogger().error("Could not read in-jar resource {}.", path);
            return null;
        }
    }

    private void writeToDisk(@NotNull String data) {
        try(FileWriter writer = new FileWriter(file)) {
            writer.append(data);
            writer.flush();
        } catch (IOException e) {
            Main.getInstance().getSLF4JLogger().error("Could not write default data to file {}.", file);
        }
    }

}
