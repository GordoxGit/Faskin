package com.heneria.skinview.debug;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.impl.MojangSkinResolver;
import com.heneria.skinview.store.FlagStore;
import com.heneria.skinview.store.YamlSkinStore;

import java.util.concurrent.atomic.AtomicLong;

/** Collects runtime metrics for /skinview debug. */
public final class DebugInfoProvider {

    private final SkinviewPlugin plugin;
    private final AtomicLong mojangHits = new AtomicLong();
    private final long startMillis = System.currentTimeMillis();

    public DebugInfoProvider(SkinviewPlugin plugin) { this.plugin = plugin; }

    public void incrementMojangHits() { mojangHits.incrementAndGet(); }

    public long mojangHits() { return mojangHits.get(); }

    public String applierName() {
        String name = plugin.applier().getClass().getSimpleName();
        if (name.contains("ProtocolLib")) return "ProtocolLib";
        if (name.contains("Paper")) return "PaperReflection";
        return "Fallback";
    }

    public int resolverCacheSize() {
        if (plugin.resolver() instanceof MojangSkinResolver r) return r.cacheSize();
        return -1;
    }

    public long resolverTtlSeconds() {
        if (plugin.resolver() instanceof MojangSkinResolver r) return r.ttlMillis() / 1000L;
        return -1L;
    }

    public int storeEntries() {
        if (plugin.store() instanceof YamlSkinStore ys) return ys.entryCount();
        return -1;
    }

    public long storeFileSize() {
        if (plugin.store() instanceof YamlSkinStore ys) return ys.fileSizeBytes();
        return -1L;
    }

    public long storeTtlSeconds() { return plugin.store().ttlSeconds(); }

    public int optOutCount() {
        FlagStore fs = plugin.flagStore();
        return fs == null ? 0 : fs.countOptOuts();
    }

    public String version() { return plugin.getDescription().getVersion(); }

    public long uptimeSeconds() { return (System.currentTimeMillis() - startMillis) / 1000L; }
}

