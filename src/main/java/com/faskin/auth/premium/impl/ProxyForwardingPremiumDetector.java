package com.faskin.auth.premium.impl;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.premium.PremiumDetector;
import com.faskin.auth.premium.PremiumEvaluation;
import com.faskin.auth.premium.PremiumMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import java.nio.charset.StandardCharsets;

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
        if (!plugin.configs().premiumEnabled()) return PremiumEvaluation.NOT_PREMIUM_UNKNOWN;
        if (plugin.configs().premiumMode() != PremiumMode.PROXY_SAFE) return PremiumEvaluation.NOT_PREMIUM_FALLBACK_MODE;

        long now = System.currentTimeMillis();
        cleanup(now);
        UUID id = e.getUniqueId();
        String ip = e.getAddress() != null ? e.getAddress().getHostAddress() : null;
        UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + e.getName()).getBytes(StandardCharsets.UTF_8));
        PremiumEvaluation eval = PremiumEvaluation.UNKNOWN;
        if (plugin.configs().premiumRequireIpForwarding() && offline.equals(id)) {
            eval = PremiumEvaluation.NOT_PREMIUM_FORWARDING_MISSING;
        }
        pending.put(id, new Entry(id, e.getName(), ip, now + TTL_MILLIS, eval));
        return eval;
    }

    @Override
    public PremiumEvaluation evaluateJoin(Player player) {
        if (!plugin.configs().premiumEnabled()) return PremiumEvaluation.NOT_PREMIUM_UNKNOWN;
        if (plugin.configs().premiumMode() != PremiumMode.PROXY_SAFE) return PremiumEvaluation.NOT_PREMIUM_FALLBACK_MODE;

        long now = System.currentTimeMillis();
        cleanup(now);
        Entry data = pending.remove(player.getUniqueId());
        if (data == null || data.expiresAt < now) {
            return plugin.configs().premiumRequireIpForwarding()
                    ? PremiumEvaluation.NOT_PREMIUM_FORWARDING_MISSING
                    : PremiumEvaluation.NOT_PREMIUM_UNKNOWN;
        }
        if (data.evaluation != PremiumEvaluation.UNKNOWN) {
            return data.evaluation;
        }

        PlayerProfile profile = player.getPlayerProfile();
        PlayerTextures textures = profile.getTextures();
        if (textures == null || textures.getSkin() == null) {
            return PremiumEvaluation.NOT_PREMIUM_NO_TEXTURES;
        }
        try {
            if (!textures.isSigned()) return PremiumEvaluation.NOT_PREMIUM_NO_TEXTURES;
        } catch (NoSuchMethodError ignored) {
            // older API without isSigned: assume valid
        }
        return PremiumEvaluation.PREMIUM_SAFE;
    }

    private void cleanup(long now) {
        pending.values().removeIf(e -> e.expiresAt < now);
    }

    private record Entry(UUID uuid, String name, String ip, long expiresAt, PremiumEvaluation evaluation) {}
}

