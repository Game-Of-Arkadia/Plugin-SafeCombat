package fr.gameofarkadia.safecombat.util;

import fr.gameofarkadia.safecombat.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class LocalStorage {

  private final File file;

  public LocalStorage(@NotNull File dataFolder) {
    this.file = new File(dataFolder, "local_data.yml");
  }

  public void save() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    // Players to kill
    List<String> playerToKill = Main.getCombatManager().getPlayersToKill().stream()
            .map(UUID::toString)
            .toList();
    config.set("players-to-kill", playerToKill);

    // Protected players
    ConfigurationSection section = config.createSection("protected-players");
    for(var entry : Main.getCombatManager().getProtectedPlayers().entrySet()) {
      section.set(entry.getKey().toString(), entry.getValue().getProtectionEnd().toEpochMilli());
    }

    try {
      config.save(file);
      Main.getInstance().getSLF4JLogger().error("Saved data properly : {} players-to-kill and {} protected.", playerToKill.size(), Main.getCombatManager().getProtectedPlayers().size());
    } catch (Exception e) {
      Main.getInstance().getSLF4JLogger().error("Could not save local-data.", e);
    }
  }

  public void reload() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    // Players to kill
    if(config.contains("players-to-kill")) {
      List<String> playersToKill = config.getStringList("players-to-kill");
      for(String player : playersToKill) {
        UUID uuid = UUID.fromString(player);
        Main.getCombatManager().addPlayerToKill(Bukkit.getOfflinePlayer(uuid));
      }
    }

    // Protected players
    Instant now = Instant.now();
    ConfigurationSection section = config.getConfigurationSection("protected-players");
    if(section != null) {
      for(String key : section.getKeys(false)) {
        UUID uuid = UUID.fromString(key);
        Instant protectionEnd = Instant.ofEpochMilli(section.getLong(key));
        if(protectionEnd.isBefore(now)) continue;

        Duration duration = Duration.between(now, protectionEnd);
        Main.getCombatManager().addPlayerProtection(Bukkit.getOfflinePlayer(uuid), duration);
      }
    }
  }

}
