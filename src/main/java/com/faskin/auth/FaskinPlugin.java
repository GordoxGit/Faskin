package com.faskin.auth;

import com.faskin.auth.commands.*;
import com.faskin.auth.config.ConfigManager;
import com.faskin.auth.core.AuthServiceRegistry;
import com.faskin.auth.core.InMemoryAccountRepository;
import com.faskin.auth.i18n.Messages;
import com.faskin.auth.security.Pbkdf2Hasher;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FaskinPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private Messages messages;
    private AuthServiceRegistry services;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.messages = new Messages(this);
        this.services = new AuthServiceRegistry(
                new InMemoryAccountRepository(new Pbkdf2Hasher())
        );

        getLogger().info("Faskin " + getDescription().getVersion() + " starting...");
        String api = getDescription().getAPIVersion();
        getLogger().info("API: " + (api != null ? api : "unknown"));

        if (configManager.isTeleportMode()) {
            String world = configManager.getSpawnWorld();
            if (Bukkit.getWorld(world) == null) {
                getLogger().warning("Monde de spawn '" + world + "' introuvable. Passez en FREEZE ou corrigez config.yml.");
            }
        }

        // Register commands (NPE-safe)
        bind("faskin", new AdminCommand(this));
        bind("register", new RegisterCommand(this));
        bind("login", new LoginCommand(this));
        bind("logout", new LogoutCommand(this));
        bind("changepassword", new ChangePasswordCommand(this));

        getLogger().info("Faskin ready in " + (System.currentTimeMillis() - start) + "ms.");
    }

    private void bind(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().severe("Commande '" + name + "' introuvable (plugin.yml).");
            return;
        }
        if (executor instanceof org.bukkit.command.CommandExecutor e) cmd.setExecutor(e);
        if (executor instanceof org.bukkit.command.TabCompleter t) cmd.setTabCompleter(t);
    }

    @Override
    public void onDisable() {
        getLogger().info("Faskin shutting down.");
    }

    public ConfigManager configs() { return configManager; }
    public Messages messages() { return messages; }
    public AuthServiceRegistry services() { return services; }
}
