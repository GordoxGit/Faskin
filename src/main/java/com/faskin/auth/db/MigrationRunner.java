package com.faskin.auth.db;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class MigrationRunner {
    private MigrationRunner() {}

    public static void run(String jdbcUrl, Logger log) {
        CompletableFuture.runAsync(() -> migrate(jdbcUrl, log)).join();
    }

    private static void migrate(String jdbcUrl, Logger log) {
        try (Connection c = DriverManager.getConnection(jdbcUrl); Statement st = c.createStatement()) {
            int oldVersion = 0;
            try (ResultSet rs = st.executeQuery("PRAGMA user_version;")) {
                if (rs.next()) oldVersion = rs.getInt(1);
            }

            Set<String> cols = new HashSet<>();
            boolean tableExists = false;
            try (ResultSet rs = st.executeQuery("PRAGMA table_info('accounts');")) {
                while (rs.next()) {
                    tableExists = true;
                    cols.add(rs.getString("name"));
                }
            }

            c.setAutoCommit(false);
            try {
                if (!tableExists) {
                    st.execute("""
                        CREATE TABLE accounts(
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
                } else {
                    if (!cols.contains("is_premium")) {
                        st.execute("ALTER TABLE accounts ADD COLUMN is_premium INTEGER DEFAULT 0;");
                        log.info("[Faskin/DB] accounts: ajout colonne is_premium");
                    }
                    if (!cols.contains("uuid_online")) {
                        st.execute("ALTER TABLE accounts ADD COLUMN uuid_online TEXT;");
                        log.info("[Faskin/DB] accounts: ajout colonne uuid_online");
                    }
                    if (!cols.contains("premium_verified_at")) {
                        st.execute("ALTER TABLE accounts ADD COLUMN premium_verified_at INTEGER;");
                        log.info("[Faskin/DB] accounts: ajout colonne premium_verified_at");
                    }
                    if (!cols.contains("premium_mode")) {
                        st.execute("ALTER TABLE accounts ADD COLUMN premium_mode TEXT;");
                        log.info("[Faskin/DB] accounts: ajout colonne premium_mode");
                    }
                }

                st.execute("CREATE INDEX IF NOT EXISTS idx_accounts_uuid_online ON accounts(uuid_online);");
                log.info("[Faskin/DB] index créé (ou existant) idx_accounts_uuid_online");
                st.execute("CREATE INDEX IF NOT EXISTS idx_accounts_is_premium ON accounts(is_premium);");
                log.info("[Faskin/DB] index créé (ou existant) idx_accounts_is_premium");
                st.execute("PRAGMA user_version = 2;");
                c.commit();
                log.info("[Faskin/DB] user_version: old=" + oldVersion + " → new=2");
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.severe("[Faskin/DB] migration failed: " + e.getMessage());
            throw new IllegalStateException("SQLite migration failed", e);
        }
    }
}
