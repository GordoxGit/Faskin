package com.faskin.auth.listeners;

import com.faskin.auth.FaskinPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public final class PremiumAsyncPreLoginListener implements Listener {
    private final FaskinPlugin plugin;

    public PremiumAsyncPreLoginListener(FaskinPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent e) {
        long start = System.nanoTime();
        var eval = plugin.services().premiumDetector().evaluatePreLogin(e);
        long ms = (System.nanoTime() - start) / 1_000_000L;
        if (plugin.configs().premiumMetrics()) {
            plugin.metrics().recordPreAuth(ms);
        }
        if (plugin.configs().premiumDebug()) {
            plugin.getLogger().info("[Faskin/Premium] prelogin name=" + e.getName() + " eval=" + eval + " time=" + ms + "ms");
        }
    }
}
