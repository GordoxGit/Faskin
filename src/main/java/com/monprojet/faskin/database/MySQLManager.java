package com.monprojet.faskin.database;

import com.monprojet.faskin.Faskin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;

public class MySQLManager {

    private final Faskin plugin;
    private Connection connection;

    public MySQLManager(Faskin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String dbName = config.getString("database.database");
        String user = config.getString("database.username");
        String pass = config.getString("database.password");

        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?autoReconnect=true",
                user,
                pass
            );

            plugin.getLogger().info("Connexion à la base de données MySQL établie avec succès !");

        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Impossible de se connecter à la base de données MySQL !");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Déconnexion de la base de données MySQL.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Erreur lors de la déconnexion de la base de données.");
                e.printStackTrace();
            }
        }
    }

    public boolean isPlayerRegistered(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM players WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la vérification de l'enregistrement du joueur.");
            e.printStackTrace();
            return false;
        }
    }

    public void registerPlayer(UUID uuid, String username, String hashedPassword, String ipAddress) {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO players (uuid, username, password_hash, last_ip, registration_timestamp) VALUES (?, ?, ?, ?, NOW())")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, username);
            statement.setString(3, hashedPassword);
            statement.setString(4, ipAddress);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'enregistrement du joueur.");
            e.printStackTrace();
        }
    }
}

