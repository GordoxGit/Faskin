package fr.projetserveur.faskin.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Central configuration handler for Faskin.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private String prefix;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the configuration and message files from disk,
     * creating default copies from the JAR if they do not yet exist.
     */
    public void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.config = load("config.yml");
        this.messages = load("messages.yml");
        this.prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", ""));
    }

    private FileConfiguration load(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Reloads the configuration and message files from disk.
     */
    public void reloadConfigs() {
        loadConfigs();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Retrieves a message from messages.yml with the prefix applied
     * and color codes translated.
     *
     * @param key message path in messages.yml
     * @return formatted message string
     */
    public String getMessage(String key) {
        String message = messages.getString(key, "");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}
