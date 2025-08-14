package com.heneria.skinview.store;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinDescriptor;
import com.heneria.skinview.service.SkinModel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class YamlSkinStore implements SkinStore {

    private final SkinviewPlugin plugin;
    private final File file;
    private final long ttlSec;
    private final ConcurrentHashMap<UUID, SkinRecord> cache = new ConcurrentHashMap<>();

    public YamlSkinStore(SkinviewPlugin plugin) {
        this.plugin = plugin;
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) dataDir.mkdirs();
        String path = plugin.getConfig().getString("storage.file", "data/skins.yml");
        this.file = new File(plugin.getDataFolder(), path);
        this.ttlSec = Math.max(300, plugin.getConfig().getLong("storage.ttl-seconds", 86400));
        load();
    }

    @Override
    public Optional<SkinRecord> get(UUID playerUuid) {
        SkinRecord rec = cache.get(playerUuid);
        if (rec == null) return Optional.empty();
        long ageSec = Instant.now().getEpochSecond() - rec.savedAtEpochSec();
        if (ageSec > ttlSec) {
            cache.remove(playerUuid);
            return Optional.empty();
        }
        return Optional.of(rec);
    }

    @Override
    public void put(UUID playerUuid, SkinDescriptor descriptor) {
        SkinRecord rec = new SkinRecord(
                descriptor.skinUrl(),
                descriptor.model(),
                descriptor.texturesValueB64(),
                descriptor.texturesSignature(),
                Instant.now().getEpochSecond()
        );
        cache.put(playerUuid, rec);
        saveAsync();
    }

    @Override
    public boolean clear(UUID playerUuid) {
        boolean removed = cache.remove(playerUuid) != null;
        if (removed) saveAsync();
        return removed;
    }

    @Override
    public long ttlSeconds() { return ttlSec; }

    /* ====== I/O YAML ====== */

    private void load() {
        if (!file.exists()) return;
        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        if (!yml.isConfigurationSection("players")) return;
        for (String k : yml.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(k);
                String url = yml.getString("players." + k + ".url");
                String model = yml.getString("players." + k + ".model", "STEVE");
                String value = yml.getString("players." + k + ".textures.value");
                String sig = yml.getString("players." + k + ".textures.signature");
                long saved = yml.getLong("players." + k + ".savedAt", 0L);
                if (url == null || saved <= 0) continue;
                SkinRecord rec = new SkinRecord(URI.create(url), SkinModel.valueOf(model), value, sig, saved);
                cache.put(uuid, rec);
            } catch (Exception ignored) {}
        }
    }

    private void saveAsync() {
        BukkitScheduler sch = plugin.getServer().getScheduler();
        sch.runTaskAsynchronously(plugin, this::saveNow);
    }

    private synchronized void saveNow() {
        try {
            YamlConfiguration yml = new YamlConfiguration();
            for (var e : cache.entrySet()) {
                String base = "players." + e.getKey();
                SkinRecord r = e.getValue();
                yml.set(base + ".url", r.skinUrl().toString());
                yml.set(base + ".model", r.model().name());
                yml.set(base + ".savedAt", r.savedAtEpochSec());
                if (r.hasSignedTextures()) {
                    yml.set(base + ".textures.value", r.texturesValueB64());
                    yml.set(base + ".textures.signature", r.texturesSignature());
                }
            }
            yml.save(file);
        } catch (Exception ex) {
            plugin.getLogger().warning("[skinview] Save store failed: " + ex.getMessage());
        }
    }
}

