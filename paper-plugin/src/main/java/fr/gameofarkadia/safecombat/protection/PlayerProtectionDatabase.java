package fr.gameofarkadia.safecombat.protection;

import fr.gameofarkadia.arkadialib.api.database.DatabaseConnection;
import fr.gameofarkadia.arkadialib.api.database.DatabaseManager;
import fr.gameofarkadia.safecombat.Main;
import fr.gameofarkadia.safecombat.storage.ToKillData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Access to database.
 */
public class PlayerProtectionDatabase {

  private final DatabaseConnection database;

  PlayerProtectionDatabase(@NotNull DatabaseManager manager) {
    this.database = manager.getDbConnection();
  }

  /**
   * Fetch all kill-orders.
   * @return a non-null list.
   */
  @NotNull List<ProtectedData> getProtectionEntries() {
    String sql = "SELECT * FROM " + dbName() + ".players_to_kill;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      ResultSet result = statement.executeQuery();
      List<ProtectedData> output = new ArrayList<>();
      while(result.next()) {
        ProtectedData data = deserializeProtectedData(result);
        output.add(data);
      }
      return output;
    } catch (SQLException e) {
      throw new RuntimeException("Could not list kill orders.", e);
    }
  }

  /**
   * Insert protection data.
   * @param data data to insert.
   */
  void insert(@NotNull ProtectedData data) {
    String sql = "INSERT INTO " + dbName() + ".protected_players (player_uuid, start_time, end_time) VALUES (?, ?, ?)" +
        " ON DUPLICATE KEY UPDATE start_time = VALUES(start_time),  end_time = VALUES(end_time);";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, data.player().toString());
      statement.setTimestamp(2, Timestamp.valueOf(data.protectionStarted()));
      statement.setTimestamp(3, Timestamp.valueOf(data.protectionFinish()));
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Could not insert protection-data " + data, e);
    }
  }

  /**
   * Delete a protection-data.
   * @param data data ton delete.
   */
  void delete(@NotNull ProtectedData data) {
    String sql = "DELETE FROM " + dbName() + ".protected_players WHERE player_uuid = ?;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, data.player().toString());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Could not delete " + data, e);
    }
  }

  private static @NotNull ProtectedData deserializeProtectedData(@NotNull ResultSet result) throws SQLException {
    return new ProtectedData(
        UUID.fromString(result.getString("player_uuid")),
        result.getTimestamp("start_time").toLocalDateTime(),
        result.getTimestamp("end_time").toLocalDateTime()
    );
  }

  private static @NotNull String dbName() {
    return Main.config().getDatabaseName();
  }

}
