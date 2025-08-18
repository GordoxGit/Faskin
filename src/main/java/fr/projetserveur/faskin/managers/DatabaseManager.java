package fr.projetserveur.faskin.managers;

import fr.projetserveur.faskin.database.IDatabase;
import fr.projetserveur.faskin.database.PlayerData;
import fr.projetserveur.faskin.database.SQLiteDatabase;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Central access point to the chosen database implementation.
 */
public class DatabaseManager {

    private final IDatabase database;

    public DatabaseManager(JavaPlugin plugin) throws SQLException {
        this.database = new SQLiteDatabase(plugin.getDataFolder());
    }

    public PlayerData loadPlayerData(UUID uniqueId) {
        return database.loadPlayerData(uniqueId);
    }

    public void savePlayerData(PlayerData data) {
        database.savePlayerData(data);
    }

    public void close() {
        if (database instanceof SQLiteDatabase sqlite) {
            sqlite.close();
        }
    }
}
