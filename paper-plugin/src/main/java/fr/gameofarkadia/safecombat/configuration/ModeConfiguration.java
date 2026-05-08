package fr.gameofarkadia.safecombat.configuration;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ModeConfiguration extends ConfigHandler {
  /**
   * New instance. Create file from default, if it does not exist.
   *
   * @param pluginFolder data-directory of the plugin.
   */
  public ModeConfiguration(@NotNull File pluginFolder) {
    super(pluginFolder, "mode.yml");
  }

  public boolean isEnabled() {
    return getData().getBoolean("enabled");
  }

}
