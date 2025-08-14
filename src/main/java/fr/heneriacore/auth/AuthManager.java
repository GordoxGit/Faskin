package fr.heneriacore.auth;

import fr.heneriacore.db.SQLiteManager;
import fr.heneriacore.event.AuthLogoutEvent;
import fr.heneriacore.event.AuthPostLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

public class AuthManager {
    private final Plugin plugin;
    private final SQLiteManager db;
    private final PasswordHasher hasher;
    private final long sessionTtl;

    private final Map<String, UUID> tokenToUuid = new ConcurrentHashMap<>();
    private final Map<UUID, String> uuidToToken = new ConcurrentHashMap<>();

    public AuthManager(Plugin plugin, SQLiteManager db, PasswordHasher hasher, long sessionTtl) {
        this.plugin = plugin;
        this.db = db;
        this.hasher = hasher;
        this.sessionTtl = sessionTtl;
        loadActiveSessions();
    }

    private void loadActiveSessions() {
        db.supplyAsync(conn -> {
            long now = Instant.now().getEpochSecond();
            try (PreparedStatement ps = conn.prepareStatement("SELECT token, uuid, expires_at FROM sessions")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long exp = rs.getLong("expires_at");
                        String token = rs.getString("token");
                        if (exp > now) {
                            UUID uuid = UUID.fromString(rs.getString("uuid"));
                            tokenToUuid.put(token, uuid);
                            uuidToToken.put(uuid, token);
                        } else {
                            try (PreparedStatement del = conn.prepareStatement("DELETE FROM sessions WHERE token=?")) {
                                del.setString(1, token);
                                del.executeUpdate();
                            }
                        }
                    }
                }
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> register(UUID uuid, String username, char[] password) {
        return db.supplyAsync(conn -> {
            try (PreparedStatement check = conn.prepareStatement("SELECT uuid FROM users WHERE uuid=? OR username=?")) {
                check.setString(1, uuid.toString());
                check.setString(2, username);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) return false;
                }
            }
            byte[] salt = PasswordHasher.generateSalt(16);
            String hash = hasher.hash(password, salt);
            Arrays.fill(password, '\0');
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users(uuid, username, password_hash, salt, created_at) VALUES(?,?,?,?,?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                ps.setString(3, hash);
                ps.setString(4, Base64.getEncoder().encodeToString(salt));
                ps.setLong(5, Instant.now().getEpochSecond());
                ps.executeUpdate();
            }
            return true;
        });
    }

    public CompletableFuture<Optional<String>> login(UUID uuid, String username, char[] password) {
        return db.supplyAsync(conn -> {
            String stored = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT password_hash FROM users WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stored = rs.getString(1);
                }
            }
            if (stored == null) return Optional.empty();
            boolean ok = hasher.verify(password, stored);
            Arrays.fill(password, '\0');
            if (!ok) return Optional.empty();
            String token = generateToken();
            long now = Instant.now().getEpochSecond();
            long exp = now + sessionTtl;
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO sessions(token, uuid, created_at, expires_at) VALUES(?,?,?,?)")) {
                ps.setString(1, token);
                ps.setString(2, uuid.toString());
                ps.setLong(3, now);
                ps.setLong(4, exp);
                ps.executeUpdate();
            }
            tokenToUuid.put(token, uuid);
            uuidToToken.put(uuid, token);
            String finalToken = token;
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new AuthPostLoginEvent(uuid)));
            return Optional.of(finalToken);
        });
    }

    public CompletableFuture<Boolean> logout(String token) {
        UUID uuid = tokenToUuid.remove(token);
        if (uuid == null) return CompletableFuture.completedFuture(false);
        uuidToToken.remove(uuid);
        return db.supplyAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sessions WHERE token=?")) {
                ps.setString(1, token);
                ps.executeUpdate();
            }
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new AuthLogoutEvent(uuid)));
            return true;
        });
    }

    public Optional<UUID> getUuidByToken(String token) {
        return Optional.ofNullable(tokenToUuid.get(token));
    }

    public Optional<String> getToken(UUID uuid) {
        return Optional.ofNullable(uuidToToken.get(uuid));
    }

    public boolean isAuthenticated(UUID uuid) {
        return uuidToToken.containsKey(uuid);
    }

    public void shutdown() {
        db.close();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
