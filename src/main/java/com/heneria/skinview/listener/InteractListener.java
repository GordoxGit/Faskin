package com.heneria.skinview.listener;

import com.heneria.skinview.SkinviewPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class InteractListener implements Listener {

    private final SkinviewPlugin plugin;

    public InteractListener(SkinviewPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        // Réservé pour interactions futures (aucune logique en Ticket 1)
    }
}
