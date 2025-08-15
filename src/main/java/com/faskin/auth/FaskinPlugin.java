package com.faskin.auth;

import com.faskin.auth.commands.*;
import com.faskin.auth.config.ConfigManager;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.AuthServiceRegistry;
import com.faskin.auth.core.InMemoryAccountRepository;
import com.faskin.auth.db.SqliteAccountRepository;
import com.faskin.auth.i18n.Messages;
import com.faskin.auth.security.Pbkdf2Hasher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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

        AccountRepository repo = switch (configManager.storageDriver()) {
            case "SQLITE", "SQLITE_INMEMORY" -> {
                File folder = getDataFolder();
                if (!folder.exists() && !folder.mkdirs()) {
                    getLogger().warning("Impossible de créer le dossier plugin: " + folder);
                }
                File db = new File(folder, "faskin.db");
                boolean inMem = "SQLITE_INMEMORY".equalsIgnoreCase(configManager.storageDriver());
                yield new SqliteAccountRepository(db, inMem, getLogger());
            }
            default -> new InMemoryAccountRepository(new Pbkdf2Hasher());
        };

        this.services = new AuthServiceRegistry(repo);

        getLogger().info("Faskin " + getDescription().getVersion() + " starting...");
        getLogger().info("API: " + java.util.Optional.ofNullable(getDescription().getAPIVersion()).orElse("unknown"));

        if (configManager.isTeleportMode()) {
            String world = configManager.getSpawnWorld();
            if (Bukkit.getWorld(world) == null) {
                getLogger().warning("Monde de spawn '" + world + "' introuvable. Passez en FREEZE ou corrigez config.yml.");
            }
        }

        bind("faskin", new AdminCommand(this));
        bind("register", new RegisterCommand(this));
        bind("login", new LoginCommand(this));
        bind("logout", new LogoutCommand(this));
        bind("changepassword", new ChangePasswordCommand(this));

        getLogger().info("Faskin ready in " + (System.currentTimeMillis() - start) + "ms.");
    }

    private void bind(String name, Object executor) {
        var cmd = getCommand(name);
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
