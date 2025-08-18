package fr.projetserveur.faskin.cache;

import fr.projetserveur.faskin.database.PlayerData;
import fr.projetserveur.faskin.managers.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple in-memory cache for player data.
 */
public class PlayerCache {

    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final DatabaseManager databaseManager;

    public PlayerCache(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public void handleJoin(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = databaseManager.loadPlayerData(uuid);
        if (data == null) {
            data = new PlayerData(uuid, player.getName(), "", false,
                    player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "");
        } else {
            data.setUsername(player.getName());
            data.setLastIpAddress(player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "");
        }
        cache.put(uuid, data);
    }

    public void handleQuit(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            databaseManager.savePlayerData(data);
        }
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            databaseManager.savePlayerData(data);
        }
        cache.clear();
    }
}
