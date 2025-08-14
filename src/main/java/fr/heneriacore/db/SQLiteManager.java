package fr.heneriacore.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.*;

public class SQLiteManager {
    @FunctionalInterface
    public interface DBTask<T> {
        T execute(Connection connection) throws SQLException;
    }

    private Connection connection;
    private ExecutorService executor;

    public void init(File dbFile) throws SQLException, IOException {
        if (executor != null) return;
        dbFile.getParentFile().mkdirs();
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        this.executor = Executors.newFixedThreadPool(2);
        runMigrations();
    }

    private void runMigrations() throws IOException, SQLException {
        File dir = new File("migrations");
        if (!dir.exists()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing(File::getName));
        try (Statement st = connection.createStatement()) {
            for (File f : files) {
                String sql = Files.readString(f.toPath());
                st.execute(sql);
            }
        }
    }

    public <T> CompletableFuture<T> supplyAsync(DBTask<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.execute(connection);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public void close() {
        if (executor != null) {
            executor.shutdown();
        }
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
        }
    }
}
