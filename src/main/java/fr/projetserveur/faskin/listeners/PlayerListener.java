package fr.projetserveur.faskin.listeners;

import fr.projetserveur.faskin.cache.PlayerCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles basic player join and quit events to manage the cache lifecycle.
 */
public class PlayerListener implements Listener {

    private final PlayerCache playerCache;

    public PlayerListener(PlayerCache playerCache) {
        this.playerCache = playerCache;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerCache.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerCache.handleQuit(event.getPlayer());
    }
}
