package fr.heneriacore;

import fr.heneriacore.auth.AuthManager;
import fr.heneriacore.auth.PasswordHasher;
import fr.heneriacore.db.SQLiteManager;
import fr.heneriacore.cmd.AuthCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class HeneriaCore extends JavaPlugin {
    private SQLiteManager sqliteManager;
    private AuthManager authManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("HeneriaCore v" + getDescription().getVersion() + " starting...");
        int threads = getConfig().getInt("auth.executor-threads", 2);
        sqliteManager = new SQLiteManager(threads);
        File dbFile = new File(getDataFolder(), getConfig().getString("auth.db", "data/heneria.db"));
        sqliteManager.init(dbFile);
        PasswordHasher hasher = new PasswordHasher();
        long ttl = getConfig().getLong("auth.session-ttl-seconds", 86400L);
        authManager = new AuthManager(this, sqliteManager, hasher, ttl);

        AuthCommand authCmd = new AuthCommand(this);
        getCommand("heneria").setExecutor(authCmd);
        getCommand("heneria").setTabCompleter(authCmd);
    }

    @Override
    public void onDisable() {
        if (authManager != null) {
            authManager.shutdown();
        }
        getLogger().info("HeneriaCore stopped.");
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
}
