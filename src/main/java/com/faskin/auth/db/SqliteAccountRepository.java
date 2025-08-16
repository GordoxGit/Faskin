package com.faskin.auth.db;

import com.faskin.auth.core.AccountRepository;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

public final class SqliteAccountRepository implements AccountRepository {
    private final String jdbcUrl;
    private final Logger log;

    public SqliteAccountRepository(File dbFile, boolean inMemory, Logger logger) {
        this.log = logger;
        this.jdbcUrl = inMemory ? "jdbc:sqlite:file:faskin_mem?mode=memory&cache=shared"
                                : "jdbc:sqlite:" + dbFile.getAbsolutePath();
        init();
    }

    private Connection get() throws SQLException { return DriverManager.getConnection(jdbcUrl); }

    private void init() {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");
            st.execute("PRAGMA foreign_keys=ON;");
            st.execute("PRAGMA busy_timeout=5000;");

            st.execute("""
                CREATE TABLE IF NOT EXISTS accounts(
                  username_ci TEXT PRIMARY KEY,
                  salt        BLOB NOT NULL,
                  hash        BLOB NOT NULL,
                  created_at  INTEGER NOT NULL,
                  last_ip     TEXT,
                  last_login  INTEGER,
                  failed_count INTEGER DEFAULT 0,
                  locked_until INTEGER,
                  is_premium  INTEGER DEFAULT 0,
                  uuid_online TEXT,
                  premium_verified_at INTEGER,
                  premium_mode TEXT
                );
            """);
            st.execute("CREATE INDEX IF NOT EXISTS idx_accounts_locked ON accounts(locked_until);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_accounts_uuid_online ON accounts(uuid_online);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_accounts_is_premium ON accounts(is_premium);");
        } catch (SQLException e) {
            log.severe("[Faskin] SQLite init failed: " + e.getMessage());
            throw new IllegalStateException("SQLite init failed", e);
        }
    }

    @Override public boolean exists(String usernameLower) {
        String sql = "SELECT 1 FROM accounts WHERE username_ci=? LIMIT 1";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            log.warning("[Faskin] exists() error: " + e.getMessage());
            return false;
        }
    }

    @Override public void create(String usernameLower, byte[] salt, byte[] hash) {
        String sql = "INSERT INTO accounts(username_ci, salt, hash, created_at) VALUES(?,?,?,?)";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            ps.setBytes(2, salt);
            ps.setBytes(3, hash);
            ps.setLong(4, Instant.now().getEpochSecond());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("create() failed: " + e.getMessage(), e);
        }
    }

    @Override public Optional<StoredAccount> find(String usernameLower) {
        String sql = "SELECT username_ci, salt, hash FROM accounts WHERE username_ci=? LIMIT 1";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new StoredAccount(rs.getString(1), rs.getBytes(2), rs.getBytes(3)));
            }
        } catch (SQLException e) {
            log.warning("[Faskin] find() error: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override public void updatePassword(String usernameLower, byte[] newSalt, byte[] newHash) {
        String sql = "UPDATE accounts SET salt=?, hash=? WHERE username_ci=?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBytes(1, newSalt);
            ps.setBytes(2, newHash);
            ps.setString(3, usernameLower);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("updatePassword() failed: " + e.getMessage(), e);
        }
    }

    @Override public void updateLastLoginAndIp(String usernameLower, String ip, long epochSeconds) {
        String sql = "UPDATE accounts SET last_ip=?, last_login=?, failed_count=0, locked_until=NULL WHERE username_ci=?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setLong(2, epochSeconds);
            ps.setString(3, usernameLower);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("[Faskin] updateLastLoginAndIp() error: " + e.getMessage());
        }
    }

    @Override public Optional<SessionMeta> getSessionMeta(String usernameLower) {
        String sql = "SELECT last_ip, last_login FROM accounts WHERE username_ci=? LIMIT 1";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new SessionMeta(rs.getString(1), rs.getLong(2)));
            }
        } catch (SQLException e) {
            log.warning("[Faskin] getSessionMeta() error: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override public boolean isLocked(String usernameLower) {
        String sql = "SELECT 1 FROM accounts WHERE username_ci=? AND locked_until IS NOT NULL AND locked_until > strftime('%s','now')";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            log.warning("[Faskin] isLocked() error: " + e.getMessage());
            return false;
        }
    }

    @Override public void registerFailedAttempt(String usernameLower, int max, long lockSeconds) {
        String failSql = """
            UPDATE accounts
               SET failed_count = COALESCE(failed_count,0) + 1,
                   locked_until = CASE WHEN failed_count + 1 >= ? THEN strftime('%s','now') + ? ELSE locked_until END,
                   failed_count = CASE WHEN failed_count + 1 >= ? THEN 0 ELSE failed_count + 1 END
             WHERE username_ci=?""";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(failSql)) {
            ps.setInt(1, max); ps.setLong(2, lockSeconds); ps.setInt(3, max); ps.setString(4, usernameLower);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("[Faskin] registerFailedAttempt() error: " + e.getMessage());
        }
    }

    @Override public void resetFailures(String usernameLower) {
        String sql = "UPDATE accounts SET failed_count=0, locked_until=NULL WHERE username_ci=?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower); ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("[Faskin] resetFailures() error: " + e.getMessage());
        }
    }

    @Override public long lockRemainingSeconds(String usernameLower) {
        String sql = "SELECT locked_until - strftime('%s','now') AS rem FROM accounts WHERE username_ci=? AND locked_until IS NOT NULL";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0L;
                long rem = rs.getLong(1);
                return Math.max(0L, rem);
            }
        } catch (SQLException e) {
            log.warning("[Faskin] lockRemainingSeconds() error: " + e.getMessage());
            return 0L;
        }
    }

    @Override public int countAccounts() {
        String sql = "SELECT COUNT(*) FROM accounts";
        try (Connection c = get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { log.warning("[Faskin] countAccounts() error: " + e.getMessage()); return 0; }
    }

    @Override public int countLockedActive(long nowEpochSeconds) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE locked_until IS NOT NULL AND locked_until > ?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, nowEpochSeconds);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { log.warning("[Faskin] countLockedActive() error: " + e.getMessage()); return 0; }
    }

    @Override public Optional<AdminInfo> adminInfo(String usernameLower) {
        String sql = "SELECT last_ip, last_login, failed_count, COALESCE(locked_until,0) FROM accounts WHERE username_ci=? LIMIT 1";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.of(new AdminInfo(false, null, 0L, 0, 0L));
                return Optional.of(new AdminInfo(true, rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getLong(4)));
            }
        } catch (SQLException e) { log.warning("[Faskin] adminInfo() error: " + e.getMessage()); return Optional.of(new AdminInfo(false, null, 0L, 0, 0L)); }
    }

    @Override public void updatePremiumInfo(String usernameLower, boolean isPremium, String uuidOnline, String premiumMode, long verifiedAtEpoch) {
        String sql = "UPDATE accounts SET is_premium=?, uuid_online=?, premium_verified_at=?, premium_mode=? WHERE username_ci=?";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, isPremium ? 1 : 0);
            if (uuidOnline != null) ps.setString(2, uuidOnline); else ps.setNull(2, Types.VARCHAR);
            ps.setLong(3, verifiedAtEpoch);
            ps.setString(4, premiumMode);
            ps.setString(5, usernameLower);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("[Faskin] updatePremiumInfo() error: " + e.getMessage());
        }
    }

    @Override public Optional<PremiumInfo> getPremiumInfo(String usernameLower) {
        String sql = "SELECT is_premium, uuid_online, premium_verified_at, premium_mode FROM accounts WHERE username_ci=? LIMIT 1";
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                boolean prem = rs.getInt(1) == 1;
                String uuid = rs.getString(2);
                long ts = rs.getLong(3);
                String mode = rs.getString(4);
                return Optional.of(new PremiumInfo(prem, uuid, ts, mode));
            }
        } catch (SQLException e) {
            log.warning("[Faskin] getPremiumInfo() error: " + e.getMessage());
            return Optional.empty();
        }
    }
}

