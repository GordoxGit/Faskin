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
        plugin.services().premiumDetector().evaluatePreLogin(e);
    }
}
