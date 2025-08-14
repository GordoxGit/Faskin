package com.heneria.skinview.listener;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinDescriptor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Spigot 1.21 — Auto-apply du skin premium au join (serveur offline).
 * - Résolution async par pseudo Mojang
 * - Application main-thread via PlayerProfile/PlayerTextures
 * - Aucun NMS, tick-safe
 */
public final class SkinAutoApplyJoinListener implements Listener {

    private final SkinviewPlugin plugin;

    public SkinAutoApplyJoinListener(SkinviewPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("apply.update-on-join", true)) return;
        if (!plugin.getConfig().getBoolean("lookups.allow-premium-name", true)) return;

        final Player player = e.getPlayer();
        final String name = player.getName();

        // Résolution async (ne JAMAIS bloquer le main thread)
        CompletableFuture<SkinDescriptor> fut = plugin.resolver().resolveByPremiumName(name);
        fut.whenComplete((sd, ex) -> {
            if (ex != null) {
                plugin.getLogger().log(Level.FINE, "[skinview] No premium skin for " + name + " (" + ex.getMessage() + ")");
                return; // rien à faire si non-premium ou échec réseau
            }
            // Application main-thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    Player p = Bukkit.getPlayer(player.getUniqueId());
                    if (p == null || !p.isOnline()) return;

                    PlayerProfile profile = p.getPlayerProfile();
                    PlayerTextures textures = profile.getTextures();

                    URL skinUrl = sd.skinUrl().toURL(); // textures.minecraft.net/...
                    textures.setSkin(skinUrl);
                    profile.setTextures(textures);
                    try {
                        // Paper: Player#setPlayerProfile(PlayerProfile) — si présent, on l’utilise
                        java.lang.reflect.Method m = Player.class.getMethod("setPlayerProfile", PlayerProfile.class);
                        m.invoke(p, profile);

                        // Petit refresh visuel facultatif (hide/show) pour forcer la propagation client
                        if (plugin.getConfig().getBoolean("apply.refresh-tablist", true)) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(p)) viewer.hidePlayer(plugin, p);
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(p)) viewer.showPlayer(plugin, p);
                                }, 2L);
                            }, 2L);
                        }
                        plugin.getLogger().fine("[skinview] Applied premium skin (Paper path)");
                    } catch (NoSuchMethodException nsme) {
                        // Spigot path: pas de setPlayerProfile dans l’API → pas d’apply live possible via API
                        plugin.getLogger().info("[skinview] Spigot runtime: API sans setPlayerProfile — pas d'application live possible (see README).");
                    } catch (Exception reflectErr) {
                        plugin.getLogger().log(Level.WARNING, "[skinview] apply failed: " + reflectErr.getMessage(), reflectErr);
                    }
                } catch (Exception err) {
                    plugin.getLogger().log(Level.WARNING, "[skinview] apply failed for " + name + ": " + err.getMessage(), err);
                }
            });
        });
    }
}

