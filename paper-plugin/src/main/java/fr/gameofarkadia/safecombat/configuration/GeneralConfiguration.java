package fr.gameofarkadia.safecombat.configuration;

import fr.gameofarkadia.safecombat.Main;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * General plugin configuration.
 */
@Getter
public class GeneralConfiguration extends ConfigHandler {

  private String language;
  private String databaseName;

  private final PvpConfiguration pvpConfiguration = new PvpConfiguration();
  private final List<String> bannedCommands = new ArrayList<>();

  /**
   * New instance. Create file from default, if it does not exist.
   * @param pluginFolder data-directory of the plugin.
   */
  public GeneralConfiguration(@NotNull File pluginFolder) {
    super(pluginFolder, "config.yml");
  }

  @Override
  protected void configReloaded(@NotNull YamlConfiguration config) {
    // Commons
    language = config.getString("language", "en");
    databaseName = config.getString("database.name", "plugin_safecombat");

    // PvP
    ConfigurationSection section = config.getConfigurationSection("pvp");
    if(section == null) {
      Main.getInstance().getLogger().warning("Missing pvp section in config.yml");
      pvpConfiguration.reload(config);
    } else {
      pvpConfiguration.reload(section);
    }

    // Banned-commands
    bannedCommands.clear();
    bannedCommands.addAll(config.getStringList("banned-commands"));
  }

  /**
   * Check if a command is banned.
   * @param command command to check.
   * @return if the command starts with a banned one.
   */
  public boolean isBanned(@NotNull String command) {
    String cmd;
    if(command.contains(":")) {
      cmd = command.split(":", 2)[1];
    } else cmd = command;
    return bannedCommands.stream().anyMatch(c -> cmd.toLowerCase().startsWith(c.toLowerCase()));
  }
}
