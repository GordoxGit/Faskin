package com.heneria.skinview.resolver;

import com.heneria.skinview.net.HttpClientWrapper;
import com.heneria.skinview.util.JsonUtils;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Best-effort resolution of Mojang-signed texture properties from a textures URL.
 */
public final class TextureSignatureResolver {

    public record SignedTexture(String value, String signature, Instant fetchedAt) {}

    private static final String HOST_TEXTURES = "textures.minecraft.net";

    private final HttpClientWrapper http;
    private volatile boolean enabled = true;
    private volatile long ttlMillis = 86_400_000L; // 24h

    private final ConcurrentHashMap<String, CacheEntry<SignedTexture>> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> hash2uuid = new ConcurrentHashMap<>();

    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder cacheMisses = new LongAdder();
    private final AtomicLong lastAttempt = new AtomicLong(0L);
    private volatile String lastError;

    public TextureSignatureResolver(HttpClientWrapper http) {
        this.http = http;
    }

    public void reload(boolean enabled, long ttlSeconds) {
        this.enabled = enabled;
        this.ttlMillis = Math.max(60, ttlSeconds) * 1000L;
    }

    /** Remember mapping and cache signed texture directly. */
    public void remember(String hash, String uuidNoDash, SignedTexture st) {
        if (hash == null || uuidNoDash == null) return;
        hash2uuid.put(hash, uuidNoDash);
        if (st != null) cache.put(hash, new CacheEntry<>(st, System.currentTimeMillis() + ttlMillis));
    }

    public CompletableFuture<Optional<SignedTexture>> resolveSignatureFromUrlAsync(URI texturesUrl) {
        if (!enabled) return CompletableFuture.completedFuture(Optional.empty());
        if (texturesUrl == null || !HOST_TEXTURES.equalsIgnoreCase(texturesUrl.getHost()))
            return CompletableFuture.completedFuture(Optional.empty());
        String hash = textureHash(texturesUrl);
        if (hash == null) return CompletableFuture.completedFuture(Optional.empty());

        CacheEntry<SignedTexture> ce = cache.get(hash);
        if (ce != null && !ce.isExpired()) {
            cacheHits.increment();
            return CompletableFuture.completedFuture(Optional.of(ce.value()));
        }

        String uuidNoDash = hash2uuid.get(hash);
        if (uuidNoDash == null) {
            cacheMisses.increment();
            lastAttempt.set(System.currentTimeMillis());
            lastError = "uuid-not-found";
            return CompletableFuture.completedFuture(Optional.empty());
        }

        lastAttempt.set(System.currentTimeMillis());
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidNoDash + "?unsigned=false";
        return http.asyncGet(URI.create(url)).handle((resp, ex) -> {
            if (ex != null) {
                lastError = ex.getMessage();
                return Optional.<SignedTexture>empty();
            }
            if (resp.statusCode() != 200) {
                lastError = "sessionserver-" + resp.statusCode();
                return Optional.<SignedTexture>empty();
            }
            var valueB64 = JsonUtils.findFirstValuePropertyBase64(resp.body());
            var signature = JsonUtils.findFirstValuePropertySignature(resp.body());
            if (valueB64.isEmpty() || signature.isEmpty()) {
                lastError = "value-or-signature-missing";
                return Optional.<SignedTexture>empty();
            }
            SignedTexture st = new SignedTexture(valueB64.get(), signature.get(), Instant.now());
            cache.put(hash, new CacheEntry<>(st, System.currentTimeMillis() + ttlMillis));
            lastError = null;
            return Optional.of(st);
        });
    }

    public void purgeExpired() { cache.entrySet().removeIf(e -> e.getValue().isExpired()); }

    public long cacheHits() { return cacheHits.longValue(); }
    public long cacheMisses() { return cacheMisses.longValue(); }
    public long lastAttemptEpochSeconds() { long ms = lastAttempt.get(); return ms == 0 ? 0 : ms / 1000L; }
    public String lastError() { return lastError; }
    public boolean isEnabled() { return enabled; }

    public static String textureHash(URI url) {
        if (url == null) return null;
        String path = url.getPath();
        int idx = path.lastIndexOf('/');
        if (idx < 0 || idx + 1 >= path.length()) return null;
        return path.substring(idx + 1);
    }

    private record CacheEntry<T>(T value, long expiryMillis) {
        boolean isExpired() { return expiryMillis < System.currentTimeMillis(); }
    }
}
