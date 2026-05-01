package fr.gameofarkadia.safecombat.connection;

import fr.gameofarkadia.arkadialib.api.ArkadiaLib;
import fr.gameofarkadia.arkadialib.api.database.DatabaseConnection;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Helper to check if a player connects to network for the first time.
 */
public class FirstPlayerConnectionHandler {

  private DatabaseConnection database;
  private final Set<UUID> seenPlayers = new HashSet<>();

  /**
   * Allows for late initialization.
   */
  public void lateInit() {
    this.database = ArkadiaLib.getDatabaseManager().getDbConnection();
  }

  /**
   * Check if a player connected to network for the "first time".
   * @param player player to look for.
   * @return a future completing with {@code true} if connecting for the first time.
   */
  public @NotNull CompletableFuture<Boolean> checkFirstPlayerConnection(@NotNull Player player) {
    if(database == null) {
      Main.logger().warn("Database connection not initialized yet, assuming player {} is NOT connecting for the first time", player.getUniqueId());
      return CompletableFuture.completedFuture(false);
    }
    if(seenPlayers.contains(player.getUniqueId()))
      return CompletableFuture.completedFuture(false);
    return SafeCombatScheduler.execAsync(() -> isItFirstPlayerConnection(player));
  }

  private boolean isItFirstPlayerConnection(@NotNull Player player) {
    if(seenPlayers.contains(player.getUniqueId()))
      return false;

    String sql = "SELECT * FROM " + Main.config().getDatabaseName() + ".seen_players WHERE player_uuid = ?;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, player.getUniqueId().toString());
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return false;
      }

      // Insert player in seen_players
      String insertSql = "INSERT INTO " + Main.config().getDatabaseName() + ".seen_players (player_uuid, player_name) VALUES (?, ?);";
      try (var insertStatement = conn.prepareStatement(insertSql)) {
        insertStatement.setString(1, player.getUniqueId().toString());
        insertStatement.setString(2, player.getName());
        insertStatement.executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException("Could not insert seen player " + player.getUniqueId(), e);
      }
      return true;
    } catch (SQLException e) {
      throw new RuntimeException("Could not check first connection for player " + player.getUniqueId(), e);
    } finally {
      seenPlayers.add(player.getUniqueId());
    }
  }

}
