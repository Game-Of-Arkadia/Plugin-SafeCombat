package fr.gameofarkadia.safecombat.bridge;

import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceHolderExpansion extends PlaceholderExpansion {

  @Override
  public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
    if(player == null) return null;

    return switch (params) {
      case "is_protected" -> String.valueOf(SafeCombatAPI.isProtected(player));
      case "is_wanted" -> String.valueOf(SafeCombatAPI.isWanted(player));
      case "is_fighting" -> String.valueOf(SafeCombatAPI.isFighting(player));
      case "icon" -> getIcon(player);
      default -> null;
    };
  }

  private @NotNull String getIcon(@NotNull OfflinePlayer player) {
    if(SafeCombatAPI.isProtected(player)) return "§b\uD83D\uDEE1";
    if(SafeCombatAPI.isWanted(player)) return "§4☠";
    if(SafeCombatAPI.isFighting(player)) return "§c⚔";
    return "";
  }

  @Override
  public @NotNull String getIdentifier() {
    return "sc";
  }

  @Override
  public @NotNull String getAuthor() {
    return "jamailun";
  }

  @Override
  public @NotNull String getVersion() {
    return Main.meta().getVersion();
  }
}
