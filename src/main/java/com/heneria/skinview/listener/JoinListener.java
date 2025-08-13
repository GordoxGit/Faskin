package com.heneria.skinview.listener;

import com.heneria.skinview.SkinviewPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {

    private final SkinviewPlugin plugin;

    public JoinListener(SkinviewPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Ticket 1 : squelette (l’apply skin sera livré dans les tickets suivants)
        plugin.getLogger().fine("Join: " + e.getPlayer().getName());
    }
}
