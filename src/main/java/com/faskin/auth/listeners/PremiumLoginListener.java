package com.faskin.auth.listeners;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.premium.PremiumEvaluation;
import com.faskin.auth.premium.PremiumEvaluatedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PremiumLoginListener implements Listener {
    private final FaskinPlugin plugin;

    public PremiumLoginListener(FaskinPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        // no-op: evaluation happens on join when profile is complete
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        player.sendMessage(plugin.messages().prefixed("premium.checking"));
        PremiumEvaluation eval = plugin.services().premiumDetector().evaluateJoin(player);
        plugin.getServer().getPluginManager().callEvent(new PremiumEvaluatedEvent(player, eval));
        if (plugin.configs().premiumMetrics()) {
            plugin.metrics().recordEvaluation(eval);
        }
        if (eval == PremiumEvaluation.PREMIUM_SAFE && plugin.configs().premiumSkipPassword()) {
            plugin.services().authBypass().markAuthenticated(player.getUniqueId(), player.getName(), player.getUniqueId().toString(), plugin.configs().premiumMode());
            player.sendMessage(plugin.messages().prefixed("premium.bypass-ok"));
            plugin.getLogger().info("[Faskin/Premium] bypass ok name=" + player.getName() + " uuidOnline=" + player.getUniqueId().toString().substring(0, 8) + " mode=" + plugin.configs().premiumMode());
        } else {
            String reasonKey = switch (eval) {
                case NOT_PREMIUM_FORWARDING_MISSING -> "forwarding-missing";
                case NOT_PREMIUM_NO_TEXTURES -> "no-textures";
                case NOT_PREMIUM_FALLBACK_MODE -> "fallback-mode";
                case NOT_PREMIUM_UNKNOWN, UNKNOWN, PREMIUM_SAFE -> "unknown";
            };
            String reason = plugin.messages().raw("premium.reason." + reasonKey);
            player.sendMessage(plugin.messages().prefixed("premium.bypass-refused").replace("{reason}", reason));
            plugin.getLogger().warning("[Faskin/Premium] bypass refused reason=" + reasonKey + " name=" + player.getName());
        }
        if (plugin.configs().premiumDebug()) {
            plugin.getLogger().info("[Faskin/Premium] debug join name=" + player.getName() + " eval=" + eval);
        }
    }
}
