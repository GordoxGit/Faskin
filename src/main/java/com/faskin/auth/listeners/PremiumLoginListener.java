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
        PremiumEvaluation eval = plugin.services().premiumDetector().evaluateJoin(player);
        plugin.getServer().getPluginManager().callEvent(new PremiumEvaluatedEvent(player, eval));

        if (eval == PremiumEvaluation.PREMIUM_SAFE && plugin.configs().premiumSkipPassword()) {
            player.sendMessage(plugin.messages().prefixed("premium.bypass-start"));
            plugin.services().authBypass().markAuthenticated(player.getUniqueId(), player.getName(), player.getUniqueId().toString(), plugin.configs().premiumMode());
            player.sendMessage(plugin.messages().prefixed("premium.bypass-ok"));
        } else if (plugin.configs().premiumRequireIpForwarding()) {
            String reason = plugin.messages().raw("premium.need-forwarding");
            player.sendMessage(plugin.messages().prefixed("premium.need-forwarding"));
            player.sendMessage(plugin.messages().prefixed("premium.bypass-refused").replace("{reason}", reason));
            plugin.getLogger().warning("Premium bypass refused for " + player.getName() + ": need-forwarding");
        }
    }
}
