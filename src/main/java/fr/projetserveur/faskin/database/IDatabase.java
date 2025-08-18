package fr.projetserveur.faskin.database;

import java.util.UUID;

public interface IDatabase {

    /**
     * Loads player data from storage.
     *
     * @param uniqueId player's unique identifier
     * @return loaded player data or null if absent
     */
    PlayerData loadPlayerData(UUID uniqueId);

    /**
     * Saves player data to storage.
     *
     * @param data player data to persist
     */
    void savePlayerData(PlayerData data);
}
