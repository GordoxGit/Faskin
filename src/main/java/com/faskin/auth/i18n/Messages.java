package com.faskin.auth.i18n;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Messages {
    private final Plugin plugin;
    private YamlConfiguration yaml;

    public Messages(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        this.yaml = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration def = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("messages.yml"), StandardCharsets.UTF_8));
        yaml.setDefaults(def);
        yaml.options().copyDefaults(true);
    }

    public String raw(String key) {
        return yaml.getString(key, "Missing message: " + key);
    }
}
