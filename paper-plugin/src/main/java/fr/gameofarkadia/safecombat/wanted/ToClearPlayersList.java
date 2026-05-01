package fr.gameofarkadia.safecombat.wanted;

import fr.gameofarkadia.arkadialib.api.ArkadiaLib;
import fr.gameofarkadia.arkadialib.api.database.DatabaseConnection;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.SafeCombatAPI;
import fr.gameofarkadia.safecombat.SafeCombatScheduler;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Helper to check if a player connects to network for the first time.
 */
public class ToClearPlayersList {

  private DatabaseConnection database;

  /**
   * Allows for late initialization.
   */
  public void lateInit() {
    this.database = ArkadiaLib.getDatabaseManager().getDbConnection();
  }

  /**
   * Check if a player connected to network for the "first time".
   * @param uuid player UUID to look for.
   * @return a future completing with {@code true} if connecting for the first time.
   */
  public @NotNull CompletableFuture<Boolean> shouldClear(@NotNull UUID uuid) {
    if(database == null) {
      Main.logger().warn("Database connection not initialized yet, assuming player {} should NOT be cleared.", uuid);
      return CompletableFuture.completedFuture(false);
    }
    return SafeCombatScheduler.execAsync(() -> shouldClearDb(uuid));
  }

  public CompletableFuture<Void> markAsClear(@NotNull UUID uuid) {
    if(database == null) {
      Main.logger().warn("Database connection not initialized yet, cannot mark player {} as clear.", uuid);
      return CompletableFuture.completedFuture(null);
    }
    return SafeCombatScheduler.execAsync(() -> writeClearDb(uuid));
  }

  private boolean shouldClearDb(@NotNull UUID uuid) {
    String sql = "DELETE FROM " + Main.config().getDatabaseName() + ".to_clear_players WHERE player_uuid = ?;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, uuid.toString());
      int deleted = statement.executeUpdate();
      return deleted > 0;
    } catch (SQLException e) {
      throw new RuntimeException("Could not check clear inv for player " + uuid, e);
    }
  }

  private void writeClearDb(@NotNull UUID uuid) {
    String sql = "INSERT INTO " + Main.config().getDatabaseName() + ".to_clear_players (player_uuid, server_id) VALUES (?,?) ON DUPLICATE KEY UPDATE server_id=?;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, uuid.toString());
      statement.setString(2, SafeCombatAPI.getServerId());
      statement.setString(3, SafeCombatAPI.getServerId());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Could not write inv-clear for player " + uuid, e);
    }
  }

}
