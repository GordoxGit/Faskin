package fr.heneriacore.prefs;

import fr.heneriacore.db.SQLiteManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PreferencesManager {
    private final SQLiteManager db;
    private final Map<UUID, PlayerPrefs> cache = new ConcurrentHashMap<>();
    private final boolean optoutDefault;
    private final boolean autoApplyDefault;

    public PreferencesManager(SQLiteManager db, boolean optoutDefault, boolean autoApplyDefault) {
        this.db = db;
        this.optoutDefault = optoutDefault;
        this.autoApplyDefault = autoApplyDefault;
    }

    public CompletableFuture<Void> setOptOut(UUID uuid, boolean optedOut) {
        PlayerPrefs prefs = cache.computeIfAbsent(uuid, u -> new PlayerPrefs(optoutDefault, autoApplyDefault));
        prefs.setOptedOut(optedOut);
        prefs.setUpdatedAt(System.currentTimeMillis());
        return db.supplyAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO player_flags(uuid,opted_out,auto_apply_skin,updated_at) VALUES(?,?,?,?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET opted_out=excluded.opted_out, auto_apply_skin=excluded.auto_apply_skin, updated_at=excluded.updated_at")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, optedOut ? 1 : 0);
                ps.setInt(3, prefs.isAutoApplySkin() ? 1 : 0);
                ps.setLong(4, prefs.getUpdatedAt());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> isOptedOut(UUID uuid) {
        PlayerPrefs cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.isOptedOut());
        }
        return db.supplyAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT opted_out, auto_apply_skin, updated_at FROM player_flags WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        PlayerPrefs prefs = new PlayerPrefs(rs.getInt(1) != 0, rs.getInt(2) != 0);
                        prefs.setUpdatedAt(rs.getLong(3));
                        cache.put(uuid, prefs);
                        return prefs.isOptedOut();
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            PlayerPrefs prefs = new PlayerPrefs(optoutDefault, autoApplyDefault);
            cache.put(uuid, prefs);
            return prefs.isOptedOut();
        });
    }

    public CompletableFuture<PlayerPrefs> getPrefs(UUID uuid) {
        PlayerPrefs cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return db.supplyAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT opted_out, auto_apply_skin, updated_at FROM player_flags WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        PlayerPrefs prefs = new PlayerPrefs(rs.getInt(1) != 0, rs.getInt(2) != 0);
                        prefs.setUpdatedAt(rs.getLong(3));
                        cache.put(uuid, prefs);
                        return prefs;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            PlayerPrefs prefs = new PlayerPrefs(optoutDefault, autoApplyDefault);
            cache.put(uuid, prefs);
            return prefs;
        });
    }

    public int countOptedOut() {
        int count = 0;
        for (PlayerPrefs prefs : cache.values()) {
            if (prefs.isOptedOut()) count++;
        }
        return count;
    }

    public static class PlayerPrefs {
        private boolean optedOut;
        private boolean autoApplySkin;
        private long updatedAt;

        public PlayerPrefs(boolean optedOut, boolean autoApplySkin) {
            this.optedOut = optedOut;
            this.autoApplySkin = autoApplySkin;
            this.updatedAt = System.currentTimeMillis();
        }

        public boolean isOptedOut() { return optedOut; }
        public void setOptedOut(boolean optedOut) { this.optedOut = optedOut; }
        public boolean isAutoApplySkin() { return autoApplySkin; }
        public void setAutoApplySkin(boolean autoApplySkin) { this.autoApplySkin = autoApplySkin; }
        public long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    }
}
