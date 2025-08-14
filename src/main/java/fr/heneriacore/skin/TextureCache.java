package fr.heneriacore.skin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TextureCache {
    private final Map<UUID, SignedTexture> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final File file;

    public TextureCache(Plugin plugin, long ttlSeconds) {
        this.ttlMillis = ttlSeconds * 1000L;
        this.file = new File(plugin.getDataFolder(), "skin-cache.yml");
        load();
    }

    public Optional<SignedTexture> get(UUID uuid) {
        SignedTexture st = cache.get(uuid);
        if (st == null) return Optional.empty();
        if (System.currentTimeMillis() - st.getFetchedAt() > ttlMillis) {
            cache.remove(uuid);
            return Optional.empty();
        }
        return Optional.of(st);
    }

    public void put(UUID uuid, SignedTexture texture) {
        cache.put(uuid, texture);
    }

    public void flush() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, SignedTexture> e : cache.entrySet()) {
            SignedTexture st = e.getValue();
            String base = e.getKey().toString();
            yaml.set(base + ".value", st.getValue());
            yaml.set(base + ".signature", st.getSignature());
            yaml.set(base + ".fetchedAt", st.getFetchedAt());
        }
        try {
            yaml.save(file);
        } catch (IOException ignored) {
        }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            String value = yaml.getString(key + ".value");
            String sig = yaml.getString(key + ".signature");
            long fetched = yaml.getLong(key + ".fetchedAt", System.currentTimeMillis());
            if (value != null) {
                cache.put(UUID.fromString(key), new SignedTexture(value, sig, fetched));
            }
        }
    }
}
