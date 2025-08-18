package fr.projetserveur.faskin.database;

import java.util.UUID;

/**
 * Represents persistent data for a player.
 */
public class PlayerData {

    private final UUID uniqueId;
    private String username;
    private String passwordHash;
    private boolean isPremium;
    private String lastIpAddress;

    public PlayerData(UUID uniqueId, String username, String passwordHash, boolean isPremium, String lastIpAddress) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isPremium = isPremium;
        this.lastIpAddress = lastIpAddress;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public String getLastIpAddress() {
        return lastIpAddress;
    }

    public void setLastIpAddress(String lastIpAddress) {
        this.lastIpAddress = lastIpAddress;
    }
}
