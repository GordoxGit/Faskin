package com.faskin.auth.i18n;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
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
        // Fichier éditable
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        this.yaml = YamlConfiguration.loadConfiguration(file);

        // Fallback par locale (resources)
        String locale = plugin.getConfig().getString("messages.locale",
                plugin.getConfig().getString("messages_locale", "fr"));
        String fallbackName = "messages_" + locale + ".yml";
        InputStream in = plugin.getResource(fallbackName);
        if (in == null) in = plugin.getResource("messages.yml"); // fallback ultime
        if (in != null) {
            YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            yaml.setDefaults(def);
            yaml.options().copyDefaults(true);
        }
    }

    public String raw(String key) {
        if (yaml.isList(key)) {
            return String.join("\n", yaml.getStringList(key));
        }
        return yaml.getString(key, "Missing message: " + key);
    }

    public String prefixed(String key) {
        String msg = raw(key);
        boolean usePrefix = plugin.getConfig().getBoolean("messages.use_prefix", true);
        String prefix = yaml.getString("prefix", "");
        String full = (usePrefix ? prefix : "") + msg;
        if (plugin.getConfig().getBoolean("messages.color_codes", true)) {
            full = ChatColor.translateAlternateColorCodes('&', full);
        }
        return full;
    }
}
