package com.faskin.auth.premium.impl;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.premium.PremiumDetector;
import com.faskin.auth.premium.PremiumEvaluation;
import com.faskin.auth.premium.PremiumMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProxyForwardingPremiumDetector implements PremiumDetector {
    private static final long TTL_MILLIS = Duration.ofMinutes(5).toMillis();

    private final FaskinPlugin plugin;
    private final Map<UUID, Entry> pending = new ConcurrentHashMap<>();

    public ProxyForwardingPremiumDetector(FaskinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PremiumEvaluation evaluatePreLogin(AsyncPlayerPreLoginEvent e) {
        if (!plugin.configs().premiumEnabled()) return PremiumEvaluation.NOT_PREMIUM;
        if (plugin.configs().premiumMode() != PremiumMode.PROXY_SAFE) return PremiumEvaluation.NOT_PREMIUM;

        long now = System.currentTimeMillis();
        cleanup(now);
        UUID id = e.getUniqueId();
        String ip = e.getAddress() != null ? e.getAddress().getHostAddress() : null;
        pending.put(id, new Entry(id, e.getName(), ip, now + TTL_MILLIS));
        return PremiumEvaluation.UNKNOWN;
    }

    @Override
    public PremiumEvaluation evaluateJoin(Player player) {
        if (!plugin.configs().premiumEnabled()) return PremiumEvaluation.NOT_PREMIUM;
        if (plugin.configs().premiumMode() != PremiumMode.PROXY_SAFE) return PremiumEvaluation.NOT_PREMIUM;

        long now = System.currentTimeMillis();
        cleanup(now);
        Entry data = pending.remove(player.getUniqueId());
        if (data == null || data.expiresAt < now) {
            return PremiumEvaluation.NOT_PREMIUM;
        }

        PlayerProfile profile = player.getPlayerProfile();
        PlayerTextures textures = profile.getTextures();
        if (textures == null || textures.getSkin() == null) {
            return PremiumEvaluation.NOT_PREMIUM;
        }
        try {
            if (!textures.isSigned()) return PremiumEvaluation.NOT_PREMIUM;
        } catch (NoSuchMethodError ignored) {
            // older API without isSigned: assume valid
        }
        return PremiumEvaluation.PREMIUM_SAFE;
    }

    private void cleanup(long now) {
        pending.values().removeIf(e -> e.expiresAt < now);
    }

    private record Entry(UUID uuid, String name, String ip, long expiresAt) {}
}

