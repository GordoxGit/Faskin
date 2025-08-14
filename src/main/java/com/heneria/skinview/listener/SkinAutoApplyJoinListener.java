package com.heneria.skinview.listener;

import com.heneria.skinview.SkinviewPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.logging.Level;

/** Auto-apply à partir du store puis fallback resolver (async) si nécessaire. */
public final class SkinAutoApplyJoinListener implements Listener {

    private final SkinviewPlugin plugin;

    public SkinAutoApplyJoinListener(SkinviewPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("apply.update-on-join", true)) return;
        final Player player = e.getPlayer();
        if (plugin.flagStore().isOptedOut(player.getUniqueId())) {
            plugin.getLogger().log(Level.FINE, "[skinview] Opt-out for " + player.getName());
            return;
        }

        // 1) Essayer le store (immédiat, main-thread)
        boolean applied = plugin.skinService().applyFromStore(Bukkit.getConsoleSender(), player);
        if (applied) return;

        // 2) Fallback: resolver async par name si autorisé
        if (!plugin.getConfig().getBoolean("lookups.allow-premium-name", true)) return;

        String name = player.getName();
        plugin.resolver().resolveByPremiumName(name).whenComplete((sd, ex) -> {
            if (ex != null) {
                plugin.getLogger().log(Level.FINE, "[skinview] No premium skin for " + name + " (" + ex.getMessage() + ")");
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    plugin.skinService().applyByPremiumName(Bukkit.getConsoleSender(), player, name);
                } catch (Exception err) {
                    plugin.getLogger().log(Level.WARNING, "[skinview] apply failed for " + name + ": " + err.getMessage(), err);
                }
            });
        });
    }
}

