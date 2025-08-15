package com.faskin.auth;

import com.faskin.auth.config.ConfigManager;
import com.faskin.auth.i18n.Messages;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FaskinPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private Messages messages;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.messages = new Messages(this);

        getLogger().info("Faskin " + getDescription().getVersion() + " starting...");
        String api = getDescription().getAPIVersion();
        getLogger().info("API: " + (api != null ? api : "unknown"));

        if (configManager.isTeleportMode()) {
            String world = configManager.getSpawnWorld();
            if (Bukkit.getWorld(world) == null) {
                getLogger().warning("Monde de spawn '" + world + "' introuvable. Passez en FREEZE ou corrigez config.yml.");
            }
        }
        getLogger().info("Faskin ready in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Faskin shutting down.");
    }

    public ConfigManager configs() { return configManager; }
    public Messages messages() { return messages; }
}
