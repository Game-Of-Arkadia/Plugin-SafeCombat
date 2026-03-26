package fr.gameofarkadia.safecombat.storage;

import fr.gameofarkadia.arkadialib.api.database.DatabaseConnection;
import fr.gameofarkadia.arkadialib.api.database.DatabaseManager;
import fr.gameofarkadia.safecombat.Main;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Access to database.
 */
public class PlayerStatesDatabase {

  private final DatabaseConnection database;

  PlayerStatesDatabase(@NotNull DatabaseManager manager) {
    this.database = manager.getDbConnection();
  }

  /**
   * Fetch all kill-orders.
   * @return a non-null list.
   */
  @NotNull List<ToKillData> getKillOrders() {
    String sql = "SELECT * FROM " + dbName() + ".players_to_kill;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      ResultSet result = statement.executeQuery();
      List<ToKillData> output = new ArrayList<>();
      while(result.next()) {
        ToKillData data = deserializeKillData(result);
        output.add(data);
      }
      return output;
    } catch (SQLException e) {
      throw new RuntimeException("Could not list kill orders.", e);
    }
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
   * Insert kill order.
   * @param data data to insert.
   */
  void insert(@NotNull ToKillData data) {
    String sql = "INSERT INTO " + dbName() + ".players_to_kill (player_uuid, server_id, disconnect_time, to_kill_time) VALUES (?, ?, ?, ?)" +
        " ON DUPLICATE KEY UPDATE server_id = VALUES(server_id), disconnected_time = VALUES(disconnected_time), to_kill_time = VALUES(to_kill_time);";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, data.player().toString());
      statement.setString(2, data.disconnectServer());
      statement.setTimestamp(3, Timestamp.valueOf(data.disconnectTime()));
      statement.setTimestamp(4, Timestamp.valueOf(data.toKillTime()));
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Could not insert kill order " + data, e);
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
   * Delete a kill-order.
   * @param data data ton delete.
   */
  void delete(@NotNull ToKillData data) {
    String sql = "DELETE FROM " + dbName() + ".players_to_kill WHERE player_uuid = ?;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, data.player().toString());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Could not delete " + data, e);
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

  /**
   * Check if a player is known in DB. Will insert it if it's not the case.
   * @param player player connecting.
   * @return true if it's the first time the player connects, false otherwise.
   */
  boolean isItFirstPlayerConnection(@NotNull Player player) {
    String sql = "SELECT * FROM " + dbName() + ".seen_players WHERE player_uuid = ?;";
    try(var conn = database.getConnection(); var statement = conn.prepareStatement(sql)) {
      statement.setString(1, player.getUniqueId().toString());
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return false;
      }

      // Insert player in seen_players
      String insertSql = "INSERT INTO " + dbName() + ".seen_players (player_uuid, player_name) VALUES (?, ?);";
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
    }
  }

  private static @NotNull ToKillData deserializeKillData(@NotNull ResultSet result) throws SQLException {
    return new ToKillData(
        UUID.fromString(result.getString("player_uuid")),
        result.getString("server_id"),
        result.getTimestamp("disconnect_time").toLocalDateTime(),
        result.getTimestamp("to_kill_time").toLocalDateTime()
    );
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
