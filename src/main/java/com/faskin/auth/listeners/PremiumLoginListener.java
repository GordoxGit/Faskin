package com.faskin.auth.listeners;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.premium.PremiumEvaluation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PremiumLoginListener implements Listener {
    private final FaskinPlugin plugin;

    public PremiumLoginListener(FaskinPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        // placeholder for future use
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        PremiumEvaluation eval = plugin.premiumCache().remove(e.getPlayer().getUniqueId());
        if (eval == PremiumEvaluation.PREMIUM_SAFE && plugin.configs().premiumSkipPassword()) {
            plugin.services().authBypass().markAuthenticated(e.getPlayer().getUniqueId(), e.getPlayer().getName(), null, plugin.configs().premiumMode());
            e.getPlayer().sendMessage(plugin.messages().prefixed("premium.bypass-ok"));
        }
    }
}
