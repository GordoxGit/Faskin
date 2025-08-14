package com.heneria.skinview.service.impl;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinApplier;
import com.heneria.skinview.service.SkinDescriptor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;

/** Applier basé sur l'API Paper (réflexion). */
public final class SkinApplierReflection implements SkinApplier {

    private final SkinviewPlugin plugin;
    private final Method setPlayerProfile;

    public SkinApplierReflection(SkinviewPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        Method m;
        try {
            m = Player.class.getMethod("setPlayerProfile", PlayerProfile.class);
            plugin.getLogger().info("Using reflection applier (Paper path)");
        } catch (NoSuchMethodException e) {
            m = null;
            plugin.getLogger().info("Spigot runtime sans ProtocolLib — apply live non disponible");
        }
        this.setPlayerProfile = m;
    }

    @Override
    public void apply(Player player, SkinDescriptor sd) {
        if (setPlayerProfile == null) return; // rien à faire
        try {
            PlayerProfile profile = player.getPlayerProfile();
            PlayerTextures textures = profile.getTextures();
            URL url = sd.skinUrl().toURL();
            textures.setSkin(url);
            profile.setTextures(textures);
            setPlayerProfile.invoke(player, profile);

            if (plugin.getConfig().getBoolean("apply.refresh-tablist", true)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.hidePlayer(plugin, player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.showPlayer(plugin, player);
                    }, 2L);
                }, 2L);
            }

            plugin.getLogger().fine("[skinview] Applied premium skin (Paper path)");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[skinview] apply failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear(Player player) {
        if (!plugin.getConfig().getBoolean("apply.refresh-tablist", true)) return;
        for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.hidePlayer(plugin, player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.showPlayer(plugin, player);
        }, 2L);
    }
}

