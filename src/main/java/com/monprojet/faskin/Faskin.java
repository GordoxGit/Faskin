package com.monprojet.faskin;

import org.bukkit.plugin.java.JavaPlugin;

public final class Faskin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Faskin a été activé. En attente de la logique d'initialisation...");
        getLogger().info("Ce plugin nécessite ProtocolLib pour fonctionner correctement.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Faskin a été désactivé. En attente de la logique de nettoyage...");
    }
}
