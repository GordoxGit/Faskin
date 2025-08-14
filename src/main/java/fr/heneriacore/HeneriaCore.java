package fr.heneriacore;

import fr.heneriacore.auth.AuthManager;
import fr.heneriacore.auth.PasswordHasher;
import fr.heneriacore.db.SQLiteManager;
import fr.heneriacore.cmd.AuthCommand;
import fr.heneriacore.premium.NameToUuidResolver;
import fr.heneriacore.premium.PremiumDetector;
import fr.heneriacore.premium.SessionProfileResolver;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class HeneriaCore extends JavaPlugin {
    private SQLiteManager sqliteManager;
    private AuthManager authManager;
    private PremiumDetector premiumDetector;

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

        if (getConfig().getBoolean("premium.enable", true)) {
            long timeout = getConfig().getLong("premium.probe.timeout-ms", 3000L);
            int rpm = getConfig().getInt("premium.probe.rate.requests_per_minute", 120);
            int burst = getConfig().getInt("premium.probe.rate.burst", 20);
            boolean fetchSig = getConfig().getBoolean("premium.fetch-signature", true);
            boolean autoLogin = getConfig().getBoolean("premium.autoLogin", true);
            Level level = Level.parse(getConfig().getString("premium.log-level", "INFO"));
            NameToUuidResolver nameRes = new NameToUuidResolver(timeout);
            SessionProfileResolver profileRes = new SessionProfileResolver(timeout, fetchSig);
            premiumDetector = new PremiumDetector(this, nameRes, profileRes, authManager, rpm, burst, autoLogin, level);
            getServer().getPluginManager().registerEvents(premiumDetector, this);
        }

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
