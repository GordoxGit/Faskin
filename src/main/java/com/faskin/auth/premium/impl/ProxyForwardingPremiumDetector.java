package com.faskin.auth.premium.impl;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.premium.PremiumDetector;
import com.faskin.auth.premium.PremiumEvaluation;
import com.faskin.auth.premium.PremiumMode;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public final class ProxyForwardingPremiumDetector implements PremiumDetector {
    private final FaskinPlugin plugin;

    public ProxyForwardingPremiumDetector(FaskinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PremiumEvaluation evaluate(AsyncPlayerPreLoginEvent e) {
        if (!plugin.configs().premiumEnabled()) return PremiumEvaluation.NOT_PREMIUM;
        PremiumMode mode = plugin.configs().premiumMode();
        if (mode != PremiumMode.PROXY_SAFE) return PremiumEvaluation.NOT_PREMIUM;
        // TODO T2.2: check forwarding UUID + signed textures from proxy
        return PremiumEvaluation.NOT_PREMIUM;
    }
}
