package com.heneria.skinview.store;

import com.heneria.skinview.SkinviewPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/** Persistant opt-out flags stored in YAML. Async I/O. */
public final class FlagStore {

    private final SkinviewPlugin plugin;
    private final File file;
    private final ConcurrentMap<UUID, Boolean> flags = new ConcurrentHashMap<>();

    public FlagStore(SkinviewPlugin plugin) {
        this.plugin = plugin;
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) dataDir.mkdirs();
        this.file = new File(dataDir, "flags.yml");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::load);
    }

    private void load() {
        if (!file.exists()) return;
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            for (String k : yml.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(k);
                    flags.put(id, yml.getBoolean(k, false));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[skinview] Failed to load flags.yml: " + e.getMessage(), e);
        }
    }

    public boolean isOptedOut(UUID id) {
        return flags.getOrDefault(id, false);
    }

    public int countOptOuts() {
        int c = 0;
        for (Boolean b : flags.values()) if (Boolean.TRUE.equals(b)) c++;
        return c;
    }

    public CompletableFuture<Void> setOptOut(UUID id, boolean optOut) {
        flags.put(id, optOut);
        CompletableFuture<Void> fut = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                saveNow();
                fut.complete(null);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "[skinview] Failed to save flags.yml: " + e.getMessage(), e);
                fut.completeExceptionally(e);
            }
        });
        return fut;
    }

    private synchronized void saveNow() throws IOException {
        YamlConfiguration yml = new YamlConfiguration();
        for (var e : flags.entrySet()) {
            yml.set(e.getKey().toString(), e.getValue());
        }
        File tmp = File.createTempFile("flags", ".yml", file.getParentFile());
        yml.save(tmp);
        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}

