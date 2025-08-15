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
        if (inMemory) {
            this.jdbcUrl = "jdbc:sqlite:file:faskin_mem?mode=memory&cache=shared";
        } else {
            this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        }
        init();
    }

    private Connection get() throws SQLException {
        // JDBC 4 charge automatiquement org.sqlite.JDBC
        return DriverManager.getConnection(jdbcUrl);
    }

    private void init() {
        try (Connection c = get();
             Statement st = c.createStatement()) {
            // Mode WAL + busy timeout pour réduire SQLITE_BUSY
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
                  locked_until INTEGER
                );
            """);
            st.execute("CREATE INDEX IF NOT EXISTS idx_accounts_locked ON accounts(locked_until);");
        } catch (SQLException e) {
            log.severe("[Faskin] SQLite init failed: " + e.getMessage());
            throw new IllegalStateException("SQLite init failed", e);
        }
    }

    @Override
    public boolean exists(String usernameLower) {
        String sql = "SELECT 1 FROM accounts WHERE username_ci=? LIMIT 1";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.warning("[Faskin] exists() error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void create(String usernameLower, byte[] salt, byte[] hash) {
        String sql = "INSERT INTO accounts(username_ci, salt, hash, created_at) VALUES(?,?,?,?)";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            ps.setBytes(2, salt);
            ps.setBytes(3, hash);
            ps.setLong(4, Instant.now().getEpochSecond());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("create() failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<StoredAccount> find(String usernameLower) {
        String sql = "SELECT username_ci, salt, hash FROM accounts WHERE username_ci=? LIMIT 1";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new StoredAccount(
                        rs.getString(1),
                        rs.getBytes(2),
                        rs.getBytes(3)
                ));
            }
        } catch (SQLException e) {
            log.warning("[Faskin] find() error: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void updatePassword(String usernameLower, byte[] newSalt, byte[] newHash) {
        String sql = "UPDATE accounts SET salt=?, hash=? WHERE username_ci=?";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBytes(1, newSalt);
            ps.setBytes(2, newHash);
            ps.setString(3, usernameLower);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("updatePassword() failed: " + e.getMessage(), e);
        }
    }

    // Bonus hooks (utiles TICKET-106) :
    public void registerFailedAttempt(String usernameLower, int max, long lockSeconds) {
        String failSql = """
            UPDATE accounts
               SET failed_count = COALESCE(failed_count,0) + 1,
                   locked_until = CASE WHEN failed_count + 1 >= ? THEN strftime('%s','now') + ? ELSE locked_until END
             WHERE username_ci=?""";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(failSql)) {
            ps.setInt(1, max);
            ps.setLong(2, lockSeconds);
            ps.setString(3, usernameLower);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("[Faskin] registerFailedAttempt() error: " + e.getMessage());
        }
    }

    public boolean isLocked(String usernameLower) {
        String sql = "SELECT 1 FROM accounts WHERE username_ci=? AND locked_until IS NOT NULL AND locked_until > strftime('%s','now')";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.warning("[Faskin] isLocked() error: " + e.getMessage());
            return false;
        }
    }

    public void resetFailures(String usernameLower) {
        String sql = "UPDATE accounts SET failed_count=0, locked_until=NULL WHERE username_ci=?";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameLower);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warning("[Faskin] resetFailures() error: " + e.getMessage());
        }
    }
}

