package fr.projetserveur.faskin.database;

import java.io.File;
import java.sql.*;
import java.util.UUID;

/**
 * SQLite implementation of the IDatabase interface.
 */
public class SQLiteDatabase implements IDatabase {

    private final Connection connection;

    public SQLiteDatabase(File dataFolder) throws SQLException {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "database.db");
        String url = "jdbc:sqlite:" + dbFile.getPath();
        this.connection = DriverManager.getConnection(url);
        createTable();
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "unique_id TEXT PRIMARY KEY, " +
                "username TEXT NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "is_premium INTEGER NOT NULL, " +
                "last_ip TEXT" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uniqueId) {
        String sql = "SELECT username, password_hash, is_premium, last_ip FROM players WHERE unique_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uniqueId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String passwordHash = rs.getString("password_hash");
                    boolean isPremium = rs.getInt("is_premium") == 1;
                    String lastIp = rs.getString("last_ip");
                    return new PlayerData(uniqueId, username, passwordHash, isPremium, lastIp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void savePlayerData(PlayerData data) {
        String sql = "INSERT INTO players (unique_id, username, password_hash, is_premium, last_ip) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT(unique_id) DO UPDATE SET " +
                "username=excluded.username, password_hash=excluded.password_hash, " +
                "is_premium=excluded.is_premium, last_ip=excluded.last_ip";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getUniqueId().toString());
            ps.setString(2, data.getUsername());
            ps.setString(3, data.getPasswordHash());
            ps.setInt(4, data.isPremium() ? 1 : 0);
            ps.setString(5, data.getLastIpAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
