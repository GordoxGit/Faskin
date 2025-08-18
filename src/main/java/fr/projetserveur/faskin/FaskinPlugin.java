package fr.projetserveur.faskin;

import org.bukkit.plugin.java.JavaPlugin;

import fr.projetserveur.faskin.managers.ConfigManager;

public class FaskinPlugin extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();
        this.getLogger().info("Faskin plugin enabled");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Faskin plugin disabled");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
