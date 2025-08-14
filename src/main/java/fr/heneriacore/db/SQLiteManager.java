package fr.heneriacore.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLiteManager {
    private final ExecutorService executor;
    private String jdbcUrl;

    public SQLiteManager(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void init(File dbFile) {
        try {
            if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            runMigrations();
        } catch (IOException e) {
            throw new RuntimeException("Failed to init SQLite database", e);
        }
    }

    private void runMigrations() throws IOException {
        Path migrationsDir = Path.of("migrations");
        if (!Files.exists(migrationsDir)) return;
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            Files.list(migrationsDir)
                    .filter(p -> p.toString().endsWith(".sql"))
                    .sorted(Comparator.naturalOrder())
                    .forEach(p -> {
                        try {
                            String sql = Files.readString(p, StandardCharsets.UTF_8);
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate(sql);
                            }
                        } catch (IOException | SQLException e) {
                            throw new RuntimeException("Migration failed: " + p, e);
                        }
                    });
        } catch (SQLException e) {
            throw new RuntimeException("Cannot run migrations", e);
        }
    }

    public <T> CompletableFuture<T> supplyAsync(DBTask<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
                return task.execute(conn);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public void close() {
        executor.shutdownNow();
    }
}
