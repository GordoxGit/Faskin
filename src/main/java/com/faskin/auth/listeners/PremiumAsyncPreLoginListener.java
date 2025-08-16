package com.faskin.auth.listeners;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.premium.PremiumEvaluation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public final class PremiumAsyncPreLoginListener implements Listener {
    private final FaskinPlugin plugin;

    public PremiumAsyncPreLoginListener(FaskinPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent e) {
        PremiumEvaluation eval = plugin.services().premiumDetector().evaluate(e);
        UUID id = e.getUniqueId();
        plugin.premiumCache().put(id, eval);
    }
}
