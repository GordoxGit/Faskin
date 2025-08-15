package com.faskin.auth.listeners;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Locale;
import com.faskin.auth.util.RateLimiter;

public final class PreAuthGuardListener implements Listener {
    private final FaskinPlugin plugin;
    private final RateLimiter limiter = new RateLimiter();

    public PreAuthGuardListener(FaskinPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isProtected(Player p) {
        if (p.hasPermission("faskin.bypass")) return false;
        return plugin.services().getState(p.getUniqueId()) != PlayerAuthState.AUTHENTICATED;
    }

    // Mouvement (cancellable -> retour à getFrom())
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        if (!plugin.configs().preBlockMovement()) return;
        Player p = e.getPlayer();
        if (isProtected(p)) {
            e.setCancelled(true);
        }
    }

    // Chat (async) -> cancel puis message sur main thread
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.configs().preBlockChat()) return;
        Player p = e.getPlayer();
        if (isProtected(p)) {
            e.setCancelled(true);
            if (limiter.shouldNotify(p.getUniqueId(), "chat", 1500)) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        p.sendMessage(plugin.messages().prefixed("blocked_chat")));
            }
        }
    }

    // Commandes: autoriser uniquement la whitelist
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (!plugin.configs().preBlockCommands()) return;
        Player p = e.getPlayer();
        if (!isProtected(p)) return;

        String msg = e.getMessage();
        if (msg.startsWith("/")) msg = msg.substring(1);
        String base = msg.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);

        if (!plugin.configs().preauthCommandWhitelist().contains(base)) {
            e.setCancelled(true);
            if (limiter.shouldNotify(p.getUniqueId(), "cmd", 1000)) {
                p.sendMessage(plugin.messages().prefixed("blocked_command"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        limiter.clear(e.getPlayer().getUniqueId());
    }

    // Interactions (clics droit/gauche sur blocs/air)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!plugin.configs().preBlockInteract()) return;
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    // Clics d'inventaire
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInv(InventoryClickEvent e) {
        if (!plugin.configs().preBlockInventory()) return;
        if (e.getWhoClicked() instanceof Player p && isProtected(p)) e.setCancelled(true);
    }

    // Lâcher d'item
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent e) {
        if (!plugin.configs().preBlockDrop()) return;
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    // Ramasser un item
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent e) {
        if (!plugin.configs().preBlockPickup()) return;
        if (e.getEntity() instanceof Player p && isProtected(p)) e.setCancelled(true);
    }

    // Swap main/offhand
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (!plugin.configs().preBlockSwap()) return;
        if (isProtected(e.getPlayer())) e.setCancelled(true);
    }

    // Dégâts reçus par un joueur non auth
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (!plugin.configs().preBlockDamageTo()) return;
        if (e.getEntity() instanceof Player p && isProtected(p)) e.setCancelled(true);
    }

    // Dégâts infligés par un joueur non auth
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamageBy(EntityDamageByEntityEvent e) {
        if (!plugin.configs().preBlockDamageFrom()) return;
        if (e.getDamager() instanceof Player p && isProtected(p)) e.setCancelled(true);
    }

    // Faim: figer la barre
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFood(FoodLevelChangeEvent e) {
        if (!plugin.configs().preBlockHunger()) return;
        if (e.getEntity() instanceof Player p && isProtected(p)) {
            e.setCancelled(true);
            p.setFoodLevel(20);
        }
    }
}

