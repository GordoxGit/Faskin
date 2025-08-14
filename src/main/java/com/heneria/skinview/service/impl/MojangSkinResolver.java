package com.heneria.skinview.service.impl;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinDescriptor;
import com.heneria.skinview.service.SkinModel;
import com.heneria.skinview.service.SkinResolver;
import com.heneria.skinview.util.JsonUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Résolution Mojang + URL textures, cache TTL + purge async. */
public final class MojangSkinResolver implements SkinResolver {
    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final String HOST_TEXTURES = "textures.minecraft.net";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final SkinviewPlugin plugin;
    private final HttpClient http;

    private volatile long ttlMillis;
    private volatile int maxEntries;
    private volatile boolean allowPremiumName;
    private volatile boolean allowTexturesUrl;
    private volatile boolean allowUnsigned;

    private final ConcurrentHashMap<String, CacheEntry<String>> name2uuid = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheEntry<SkinDescriptor>> uuid2skin = new ConcurrentHashMap<>();

    private BukkitTask purgeTask;

    public MojangSkinResolver(SkinviewPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.http = HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build();
        reloadSettings();
        startPurge();
    }

    @Override
    public CompletableFuture<SkinDescriptor> resolveByPremiumName(String name) {
        if (!allowPremiumName) return CompletableFuture.failedFuture(new IllegalArgumentException("Premium-name lookup disabled"));
        if (name == null || !NAME.matcher(name).matches())
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid premium name"));
        final String key = name.toLowerCase();

        CacheEntry<String> ce = name2uuid.get(key);
        if (ce != null && !ce.isExpired()) return resolveByUuid(ce.value());

        final String url = "https://api.mojang.com/users/profiles/minecraft/" + key;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT)
                .header("User-Agent", "skinview/" + plugin.getDescription().getVersion())
                .GET().build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenCompose(resp -> {
                    if (resp.statusCode() != 200)
                        return CompletableFuture.failedFuture(new IllegalStateException("Mojang name lookup failed (" + resp.statusCode() + ")"));
                    final Optional<String> uuidNoDash = JsonUtils.findString(resp.body(), "id");
                    if (uuidNoDash.isEmpty() || uuidNoDash.get().length() != 32)
                        return CompletableFuture.failedFuture(new IllegalStateException("Invalid UUID in response"));
                    putNameUuid(key, uuidNoDash.get());
                    return resolveByUuid(uuidNoDash.get());
                });
    }

    @Override
    public CompletableFuture<SkinDescriptor> resolveByTexturesUrl(String url) {
        if (!allowTexturesUrl) return CompletableFuture.failedFuture(new IllegalArgumentException("Textures-URL lookup disabled"));
        try {
            URI u = URI.create(url);
            if (!HOST_TEXTURES.equalsIgnoreCase(u.getHost()))
                return CompletableFuture.failedFuture(new IllegalArgumentException("Only textures.minecraft.net allowed"));
            return CompletableFuture.completedFuture(new SkinDescriptor(u, SkinModel.STEVE, null, null));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid textures URL", e));
        }
    }

    private CompletableFuture<SkinDescriptor> resolveByUuid(String uuidNoDash) {
        CacheEntry<SkinDescriptor> ce = uuid2skin.get(uuidNoDash);
        if (ce != null && !ce.isExpired()) return CompletableFuture.completedFuture(ce.value());

        final String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidNoDash;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT)
                .header("User-Agent", "skinview/" + plugin.getDescription().getVersion())
                .GET().build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() != 200)
                        throw new CompletionException(new IllegalStateException("Sessionserver failed (" + resp.statusCode() + ")"));

                    final String body = resp.body();
                    final Optional<String> valueB64 = JsonUtils.findFirstValuePropertyBase64(body);
                    final Optional<String> signature = JsonUtils.findFirstValuePropertySignature(body);
                    if (valueB64.isEmpty() || signature.isEmpty())
                        throw new CompletionException(new IllegalStateException("textures value/signature missing"));

                    final String texJson = new String(Base64.getDecoder().decode(valueB64.get()));
                    final Optional<String> skinUrl = JsonUtils.findSkinUrl(texJson);
                    if (skinUrl.isEmpty()) throw new CompletionException(new IllegalStateException("SKIN.url missing"));
                    final Optional<String> model = JsonUtils.findSkinModel(texJson);

                    SkinDescriptor sd = new SkinDescriptor(
                            URI.create(skinUrl.get()),
                            SkinModel.fromMetadata(model.orElse(null)),
                            valueB64.get(),
                            signature.get()
                    );
                    putUuidSkin(uuidNoDash, sd);
                    return sd;
                });
    }

    private void startPurge() {
        long periodTicks = Math.max(40, (ttlMillis / 2) / 50); // ~TTL/2, min ~2s
        this.purgeTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::purgeExpired, periodTicks, periodTicks);
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        name2uuid.entrySet().removeIf(e -> e.getValue().expiryMillis < now);
        uuid2skin.entrySet().removeIf(e -> e.getValue().expiryMillis < now);
        int max = maxEntries;
        if (name2uuid.size() > max) trim(name2uuid, max);
        if (uuid2skin.size() > max) trim(uuid2skin, max);
    }

    private <T> void trim(ConcurrentHashMap<String, CacheEntry<T>> map, int max) {
        int target = (int) (max * 0.9);
        map.entrySet().stream()
                .sorted((a, b) -> Long.compare(a.getValue().expiryMillis, b.getValue().expiryMillis))
                .limit(Math.max(0, map.size() - target))
                .forEach(e -> map.remove(e.getKey()));
    }

    private void putNameUuid(String name, String uuidNoDash) {
        name2uuid.put(name, new CacheEntry<>(uuidNoDash, System.currentTimeMillis() + ttlMillis));
    }

    private void putUuidSkin(String uuidNoDash, SkinDescriptor sd) {
        uuid2skin.put(uuidNoDash, new CacheEntry<>(sd, System.currentTimeMillis() + ttlMillis));
    }

    @Override
    public void reloadSettings() {
        FileConfiguration c = plugin.getConfig();
        long ttlSec = Math.max(60, c.getLong("cache.ttl-seconds", 3600));
        this.ttlMillis = ttlSec * 1000L;
        this.maxEntries = Math.max(50, c.getInt("cache.max-entries", 500));
        this.allowPremiumName = c.getBoolean("lookups.allow-premium-name", true);
        this.allowTexturesUrl = c.getBoolean("lookups.allow-textures-url", true);
        this.allowUnsigned = c.getBoolean("lookups.allow-unsigned", true);
        plugin.getLogger().info(String.format("SkinResolver: ttl=%ds, max=%d, premium=%s, url=%s",
                ttlSec, maxEntries, allowPremiumName, allowTexturesUrl));
    }

    @Override
    public void shutdown() {
        if (purgeTask != null) { purgeTask.cancel(); purgeTask = null; }
        name2uuid.clear();
        uuid2skin.clear();
    }

    private record CacheEntry<T>(T value, long expiryMillis) {
        boolean isExpired() { return expiryMillis < System.currentTimeMillis(); }
    }
}
