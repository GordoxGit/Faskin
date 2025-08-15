package com.faskin.auth.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public final class ConfigManager {
    private final FileConfiguration cfg;

    public ConfigManager(Plugin plugin) {
        this.cfg = plugin.getConfig();
    }

    public boolean isTeleportMode() {
        String mode = cfg.getString("spawn.mode", "FREEZE");
        return "TELEPORT".equalsIgnoreCase(mode);
    }
    public String getSpawnWorld() { return cfg.getString("spawn.world", "world"); }
    public int loginTimeoutSeconds() { return cfg.getInt("login.timeout_seconds", 45); }
    public int passwordMinLength() { return cfg.getInt("password.min_length", 8); }
    public boolean requireDigit() { return cfg.getBoolean("password.require_digit", true); }
    public boolean requireLetter() { return cfg.getBoolean("password.require_letter", true); }
}
