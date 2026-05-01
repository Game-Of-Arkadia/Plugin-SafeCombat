package fr.gameofarkadia.safecombat.configuration;

import fr.gameofarkadia.arkadialib.api.utils.DurationWrapper;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Getter
public class PunishmentConfiguration {

  private boolean loseMoneyEnabled;

  private boolean banEnabled;
  private DurationWrapper banDuration;
  private String banReason;
  private String banMainServer;

  public void reload(@NotNull ConfigurationSection config) {
    loseMoneyEnabled = config.getBoolean("lose-money", false);

    banEnabled = config.getBoolean("ban.enabled", false);
    banReason = config.getString("ban.reason", "Déconnecté en combat.");
    banDuration = DurationWrapper.parse(config.getString("ban.duration", "1d"));
    banMainServer = config.getString("ban.main-server", "arkadia");
  }

}
