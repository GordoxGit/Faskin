package fr.heneriacore.auth;

import fr.heneriacore.db.SQLiteManager;
import fr.heneriacore.event.AuthLogoutEvent;
import fr.heneriacore.event.AuthPostLoginEvent;
import fr.heneriacore.event.AuthPreLoginEvent;
import fr.heneriacore.premium.GameProfile;
import fr.heneriacore.premium.PremiumAuthService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager implements PremiumAuthService {
    private final Plugin plugin;
    private final SQLiteManager db;
    private final PasswordHasher hasher;
    private final long sessionTtlMillis;
    private final Map<String, UUID> tokenToUuid = new ConcurrentHashMap<>();
    private final Map<UUID, String> uuidToToken = new ConcurrentHashMap<>();
    private final java.security.SecureRandom random = new java.security.SecureRandom();

    public AuthManager(Plugin plugin, SQLiteManager db, PasswordHasher hasher, long sessionTtlSeconds) {
        this.plugin = plugin;
        this.db = db;
        this.hasher = hasher;
        this.sessionTtlMillis = sessionTtlSeconds * 1000L;
    }

    public CompletableFuture<Boolean> register(UUID uuid, String username, char[] password) {
        return db.supplyAsync(conn -> {
            String existsSql = "SELECT 1 FROM users WHERE uuid = ? OR username = ?";
            try (var ps = conn.prepareStatement(existsSql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            String hash = hasher.hash(password, salt);
            String stored = Base64.getEncoder().encodeToString(salt) + ":" + hash;
            java.util.Arrays.fill(password, '\0');
            String insertSql = "INSERT INTO users(uuid, username, password_hash, salt, created_at) VALUES(?,?,?,?,?)";
            try (var ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                ps.setString(3, stored);
                ps.setString(4, Base64.getEncoder().encodeToString(salt));
                ps.setLong(5, System.currentTimeMillis());
                ps.executeUpdate();
                return true;
            }
        });
    }

    public CompletableFuture<Optional<String>> login(UUID uuid, String username, char[] password) {
        Bukkit.getPluginManager().callEvent(new AuthPreLoginEvent(uuid, username));
        return db.supplyAsync(conn -> {
            String select = "SELECT password_hash FROM users WHERE uuid = ? OR username = ?";
            try (var ps = conn.prepareStatement(select)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                try (var rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.<String>empty();
                    }
                    String stored = rs.getString("password_hash");
                    if (!hasher.verify(password, stored)) {
                        return Optional.<String>empty();
                    }
                }
            }
            java.util.Arrays.fill(password, '\0');
            String token = generateToken();
            long now = System.currentTimeMillis();
            long expires = now + sessionTtlMillis;
            String insert = "INSERT INTO sessions(token, uuid, created_at, expires_at) VALUES(?,?,?,?)";
            try (var ps = conn.prepareStatement(insert)) {
                ps.setString(1, token);
                ps.setString(2, uuid.toString());
                ps.setLong(3, now);
                ps.setLong(4, expires);
                ps.executeUpdate();
            }
            tokenToUuid.put(token, uuid);
            uuidToToken.put(uuid, token);
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.getPluginManager().callEvent(new AuthPostLoginEvent(uuid, username, token)));
            return Optional.of(token);
        });
    }

    @Override
    public CompletableFuture<Boolean> autoLogin(UUID uuid, GameProfile profile) {
        return db.supplyAsync(conn -> {
            String exists = "SELECT 1 FROM users WHERE uuid = ?";
            try (var ps = conn.prepareStatement(exists)) {
                ps.setString(1, uuid.toString());
                try (var rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        String insertUser = "INSERT INTO users(uuid, username, password_hash, salt, created_at) VALUES(?,?,?,?,?)";
                        try (var ins = conn.prepareStatement(insertUser)) {
                            ins.setString(1, uuid.toString());
                            ins.setString(2, profile.getName());
                            ins.setString(3, "");
                            ins.setString(4, "");
                            ins.setLong(5, System.currentTimeMillis());
                            ins.executeUpdate();
                        }
                    }
                }
            }
            String token = generateToken();
            long now = System.currentTimeMillis();
            long expires = now + sessionTtlMillis;
            String insert = "INSERT INTO sessions(token, uuid, created_at, expires_at) VALUES(?,?,?,?)";
            try (var ps = conn.prepareStatement(insert)) {
                ps.setString(1, token);
                ps.setString(2, uuid.toString());
                ps.setLong(3, now);
                ps.setLong(4, expires);
                ps.executeUpdate();
            }
            tokenToUuid.put(token, uuid);
            uuidToToken.put(uuid, token);
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.getPluginManager().callEvent(new AuthPostLoginEvent(uuid, profile.getName(), token)));
            return true;
        });
    }

    public CompletableFuture<Boolean> logout(String token) {
        return db.supplyAsync(conn -> {
            String delete = "DELETE FROM sessions WHERE token = ?";
            try (var ps = conn.prepareStatement(delete)) {
                ps.setString(1, token);
                int updated = ps.executeUpdate();
                UUID u = tokenToUuid.remove(token);
                if (u != null) {
                    uuidToToken.remove(u);
                    UUID uuidFinal = u;
                    Bukkit.getScheduler().runTask(plugin, () ->
                            Bukkit.getPluginManager().callEvent(new AuthLogoutEvent(uuidFinal, token)));
                }
                return updated > 0;
            }
        });
    }

    public Optional<UUID> getUuidByToken(String token) {
        UUID cached = tokenToUuid.get(token);
        if (cached != null) return Optional.of(cached);
        try {
            return db.supplyAsync(conn -> {
                String sql = "SELECT uuid, expires_at FROM sessions WHERE token = ?";
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setString(1, token);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long expires = rs.getLong("expires_at");
                            if (expires < System.currentTimeMillis()) {
                                try (var del = conn.prepareStatement("DELETE FROM sessions WHERE token = ?")) {
                                    del.setString(1, token);
                                    del.executeUpdate();
                                }
                                return Optional.<UUID>empty();
                            }
                            UUID u = UUID.fromString(rs.getString("uuid"));
                            tokenToUuid.put(token, u);
                            uuidToToken.put(u, token);
                            return Optional.of(u);
                        }
                        return Optional.<UUID>empty();
                    }
                }
            }).join();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isAuthenticated(UUID uuid) {
        return uuidToToken.containsKey(uuid);
    }

    public Optional<String> getToken(UUID uuid) {
        return Optional.ofNullable(uuidToToken.get(uuid));
    }

    public void shutdown() {
        db.close();
    }

    private String generateToken() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
