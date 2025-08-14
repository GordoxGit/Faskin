package fr.heneriacore.premium;

import fr.heneriacore.premium.event.PremiumLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class PremiumDetector implements Listener {
    private final Plugin plugin;
    private final NameToUuidResolver nameResolver;
    private final SessionProfileResolver profileResolver;
    private final PremiumAuthService authService;
    private final fr.heneriacore.prefs.PreferencesManager prefs;
    private final TokenBucket rateLimiter;
    private final boolean autoLogin;
    private final Level logLevel;
    private final AtomicInteger failures = new AtomicInteger();
    private volatile long circuitUntil = 0L;
    private final long circuitCooldown = 60000L;
    private final int maxRetries = 3;

    public PremiumDetector(Plugin plugin, NameToUuidResolver nameResolver, SessionProfileResolver profileResolver,
                           PremiumAuthService authService, int rpm, int burst, boolean autoLogin, Level logLevel,
                           fr.heneriacore.prefs.PreferencesManager prefs) {
        this.plugin = plugin;
        this.nameResolver = nameResolver;
        this.profileResolver = profileResolver;
        this.authService = authService;
        this.rateLimiter = new TokenBucket(rpm, burst);
        this.autoLogin = autoLogin;
        this.logLevel = logLevel;
        this.prefs = prefs;
    }

    public CompletableFuture<Optional<GameProfile>> detectByNameAsync(String name) {
        long now = System.currentTimeMillis();
        if (now < circuitUntil) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        if (!rateLimiter.tryConsume()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    Optional<String> uuidOpt = nameResolver.nameToUuid(name).get();
                    if (uuidOpt.isEmpty()) return Optional.empty();
                    Optional<GameProfile> profileOpt = profileResolver.fetchProfileForUuid(uuidOpt.get()).get();
                    if (profileOpt.isPresent()) {
                        failures.set(0);
                        if (logLevel.intValue() <= Level.INFO.intValue()) {
                            plugin.getLogger().log(Level.INFO, "Premium detected for " + name);
                        }
                        return profileOpt;
                    }
                } catch (Exception e) {
                    if (logLevel.intValue() <= Level.FINE.intValue()) {
                        plugin.getLogger().log(Level.FINE, "Premium detection retry", e);
                    }
                }
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 100L);
                } catch (InterruptedException ignored) { }
            }
            int fail = failures.incrementAndGet();
            if (fail >= 5) {
                circuitUntil = System.currentTimeMillis() + circuitCooldown;
                plugin.getLogger().log(Level.WARNING, "PremiumDetector circuit opened");
            }
            return Optional.empty();
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        detectByNameAsync(player.getName()).thenAccept(opt -> opt.ifPresent(profile -> {
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.getPluginManager().callEvent(new PremiumLoginEvent(player, profile)));
            if (autoLogin) {
                prefs.isOptedOut(player.getUniqueId()).thenAccept(out -> {
                    if (!out) {
                        authService.autoLogin(UUID.fromString(profile.getUuid()), profile);
                    }
                });
            }
        }));
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillPerMillis;
        private double tokens;
        private long lastRefill;

        TokenBucket(int requestsPerMinute, int burst) {
            this.capacity = burst;
            this.tokens = burst;
            this.refillPerMillis = requestsPerMinute / 60000.0;
            this.lastRefill = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            double add = (now - lastRefill) * refillPerMillis;
            if (add > 0) {
                tokens = Math.min(capacity, tokens + add);
                lastRefill = now;
            }
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }
    }
}
