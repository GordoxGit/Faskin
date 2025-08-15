package com.faskin.auth.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ConfigManager {
    private final FileConfiguration cfg;

    public ConfigManager(Plugin plugin) { this.cfg = plugin.getConfig(); }

    public boolean isTeleportMode() {
        String mode = cfg.getString("spawn.mode", "FREEZE");
        return "TELEPORT".equalsIgnoreCase(mode);
    }
    public String getSpawnWorld() { return cfg.getString("spawn.world", "world"); }

    // Login / sessions / bruteforce / timeout
    public int loginTimeoutSeconds() { return cfg.getInt("login.timeout_seconds", 45); }
    public boolean kickOnTimeout() { return cfg.getBoolean("login.kick_on_timeout", true); }
    public boolean allowIpSession() { return cfg.getBoolean("login.allow_ip_session", true); }
    public int sessionMinutes() { return cfg.getInt("login.session_minutes", 30); }
    public int maxFailedAttempts() { return cfg.getInt("login.max_failed_attempts", 5); }
    public int lockMinutes() { return cfg.getInt("login.lock_minutes", 15); }
    public int minSecondsBetweenAttempts() { return cfg.getInt("login.min_seconds_between_attempts", 2); }

    // Password rules
    public int passwordMinLength() { return cfg.getInt("password.min_length", 8); }
    public boolean requireDigit() { return cfg.getBoolean("password.require_digit", true); }
    public boolean requireLetter() { return cfg.getBoolean("password.require_letter", true); }

    // Storage
    public String storageDriver() { return cfg.getString("storage.driver", "SQLITE"); }

    // --- Pré-auth: toggles ---
    public boolean preBlockMovement() { return cfg.getBoolean("preauth.block.movement", true); }
    public boolean preBlockChat() { return cfg.getBoolean("preauth.block.chat", true); }
    public boolean preBlockCommands() { return cfg.getBoolean("preauth.block.commands", true); }
    public boolean preBlockInteract() { return cfg.getBoolean("preauth.block.interact", true); }
    public boolean preBlockInventory() { return cfg.getBoolean("preauth.block.inventory_click", true); }
    public boolean preBlockDrop() { return cfg.getBoolean("preauth.block.item_drop", true); }
    public boolean preBlockPickup() { return cfg.getBoolean("preauth.block.item_pickup", true); }
    public boolean preBlockSwap() { return cfg.getBoolean("preauth.block.swap_hand", true); }
    public boolean preBlockDamageTo() { return cfg.getBoolean("preauth.block.damage_to_player", true); }
    public boolean preBlockDamageFrom() { return cfg.getBoolean("preauth.block.damage_from_player", true); }
    public boolean preBlockHunger() { return cfg.getBoolean("preauth.block.hunger", true); }

    public Set<String> preauthCommandWhitelist() {
        List<String> list = cfg.getStringList("preauth.commands.whitelist");
        Set<String> out = new HashSet<>();
        for (String s : list) out.add(s.toLowerCase(Locale.ROOT));
        // garde-fous impératifs
        out.add("register");
        out.add("login");
        return out;
    }
}

