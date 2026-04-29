package fr.gameofarkadia.safecombat;

import fr.gameofarkadia.safecombat.combat.CombatManager;
import fr.gameofarkadia.safecombat.protection.ProtectionManager;
import fr.gameofarkadia.safecombat.wanted.WantedPlayersManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * Safe combat plugin.
 */
@ApiStatus.Internal
public interface SafeCombatPlugin {

  @NotNull String getServerId();

  @NotNull WantedPlayersManager getWantedPlayersManager();

  @NotNull CombatManager getCombatManager();

  @NotNull ProtectionManager getProtectionManager();

}
