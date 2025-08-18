package fr.projetserveur.faskin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import fr.projetserveur.faskin.managers.ConfigManager;
import fr.projetserveur.faskin.managers.DatabaseManager;
import fr.projetserveur.faskin.cache.PlayerCache;
import fr.projetserveur.faskin.listeners.PlayerListener;

public class FaskinPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerCache playerCache;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        try {
            this.databaseManager = new DatabaseManager(this);
            this.playerCache = new PlayerCache(databaseManager);
        } catch (Exception e) {
            getLogger().severe("Unable to initialise database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(playerCache), this);

        this.getLogger().info("Faskin plugin enabled");
    }

    @Override
    public void onDisable() {
        if (playerCache != null) {
            playerCache.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        this.getLogger().info("Faskin plugin disabled");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerCache getPlayerCache() {
        return playerCache;
    }
}
