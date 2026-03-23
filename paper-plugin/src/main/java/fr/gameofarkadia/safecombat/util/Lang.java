package fr.gameofarkadia.safecombat.util;

import fr.gameofarkadia.safecombat.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class Lang {

    private final YamlConfiguration langConfig;

    public Lang(@NotNull String lang) {
        File customLangFile = new File(Main.getInstance().getDataFolder(), "lang/" + lang + ".yml");
        File langFile;
        YamlConfiguration defaultConfiguration;

        if(!customLangFile.exists()) {
            Util.console("§cSpecified language file doesn't exist. EN language used by default.");
            langFile = new File(Main.getInstance().getDataFolder(), "lang/en.yml");
            defaultConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(Main.getInstance().getResource("lang/en.yml"), StandardCharsets.UTF_8));
        } else {
            langFile = customLangFile;
            defaultConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(Main.getInstance().getResource("lang/" + lang + ".yml"), StandardCharsets.UTF_8));
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Set defaults
        langConfig.addDefaults(defaultConfiguration.getConfigurationSection("").getValues(true));
        langConfig.options().copyDefaults(true);
        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a translation.
     * @param path key.
     * @return a non-nulll string.
     */
    public @NotNull String get(@NotNull String path) {
        return langConfig.getString(path, "??" + path + "??").replaceAll("&", "§");
    }

    public static void setupFiles() {
        File langDirectory = new File(Main.getInstance().getDataFolder(), "lang");
        if(!langDirectory.exists()) langDirectory.mkdirs();

        try {
            walkResources(Main.getInstance().getClass(), "/lang", 1, path -> {
                String localeFileName = path.getFileName().toString();
                if (!localeFileName.toLowerCase().endsWith(".yml")) return;

                if (!Files.exists(Main.getInstance().getDataFolder().toPath().resolve("lang").resolve(localeFileName))) {
                    Main.getInstance().saveResource("lang/" + localeFileName, false);
                }
            });
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method from @SkyAsul in SpigotMC forum
     * <a href="https://www.spigotmc.org/threads/save-all-resources-from-lang-folder.586221/">...</a>
     */
    public static void walkResources(Class<?> clazz, String path, int depth, Consumer<Path> consumer) throws URISyntaxException, IOException {
        URI uri = clazz.getResource(path).toURI();
        FileSystem fileSystem = null;
        Path myPath;
        try {
            if (uri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath(path);
            } else {
                myPath = Paths.get(uri);
            }

            try (Stream<Path> walker = Files.walk(myPath, depth)) {
                walker.forEach(consumer);
            }
        } finally {
            if (fileSystem != null) fileSystem.close();
        }
    }

}