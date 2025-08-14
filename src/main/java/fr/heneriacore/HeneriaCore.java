package fr.heneriacore;

import fr.heneriacore.auth.AuthManager;
import fr.heneriacore.auth.PasswordHasher;
import fr.heneriacore.cmd.AuthCommand;
import fr.heneriacore.db.SQLiteManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class HeneriaCore extends JavaPlugin {
    private SQLiteManager sqlite;
    private AuthManager authManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("HeneriaCore v" + getDescription().getVersion() + " starting...");
        sqlite = new SQLiteManager();
        try {
            File dbFile = new File(getConfig().getString("auth.db", "data/heneria.db"));
            sqlite.init(dbFile);
        } catch (Exception e) {
            getLogger().severe("Failed to init DB: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PasswordHasher hasher = new PasswordHasher(getConfig().getString("auth.hash", "pbkdf2"));
        long ttl = getConfig().getLong("auth.session-ttl-seconds", 86400L);
        authManager = new AuthManager(this, sqlite, hasher, ttl);
        AuthCommand cmd = new AuthCommand(this, authManager);
        getCommand("heneria").setExecutor(cmd);
        getCommand("heneria").setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {
        if (authManager != null) authManager.shutdown();
        getLogger().info("HeneriaCore stopped.");
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
}
