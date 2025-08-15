package fr.heneriacore;

import fr.heneriacore.auth.AuthManager;
import fr.heneriacore.auth.PasswordHasher;
import fr.heneriacore.db.SQLiteManager;
import fr.heneriacore.cmd.AuthCommand;
import fr.heneriacore.cmd.ClaimCommand;
import fr.heneriacore.cmd.PreferencesCommand;
import fr.heneriacore.cmd.DebugCommand;
import fr.heneriacore.claim.ClaimManager;
import fr.heneriacore.prefs.PreferencesManager;
import fr.heneriacore.premium.NameToUuidResolver;
import fr.heneriacore.premium.PremiumDetector;
import fr.heneriacore.premium.SessionProfileResolver;
import fr.heneriacore.skin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Level;

public final class HeneriaCore extends JavaPlugin {
    private SQLiteManager sqliteManager;
    private AuthManager authManager;
    private PremiumDetector premiumDetector;
    private TextureCache textureCache;
    private SkinServiceImpl skinService;
    private ClaimManager claimManager;
    private PreferencesManager preferencesManager;

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
        boolean optDefault = getConfig().getBoolean("preferences.optout_default", false);
        boolean autoApplyDefault = getConfig().getBoolean("preferences.default_auto_apply_skin", true);
        preferencesManager = new PreferencesManager(sqliteManager, optDefault, autoApplyDefault);

        if (getConfig().getBoolean("premium.enable", true)) {
            long timeout = getConfig().getLong("premium.probe.timeout-ms", 3000L);
            int rpm = getConfig().getInt("premium.probe.rate.requests_per_minute", 120);
            int burst = getConfig().getInt("premium.probe.rate.burst", 20);
            boolean fetchSig = getConfig().getBoolean("premium.fetch-signature", true);
            boolean autoLogin = getConfig().getBoolean("premium.autoLogin", true);
            Level level = Level.parse(getConfig().getString("premium.log-level", "INFO"));
            NameToUuidResolver nameRes = new NameToUuidResolver(timeout);
            SessionProfileResolver profileRes = new SessionProfileResolver(timeout, fetchSig);
            premiumDetector = new PremiumDetector(this, nameRes, profileRes, authManager, rpm, burst, autoLogin, level, preferencesManager);
            getServer().getPluginManager().registerEvents(premiumDetector, this);
        }

        long claimTtl = getConfig().getLong("claim.ttl-seconds", 900L);
        claimManager = new ClaimManager(this, claimTtl);
        ClaimCommand claimCmd = new ClaimCommand(this, claimManager);

        long skinTtl = getConfig().getLong("skin.cache-ttl-seconds", 86400L);
        boolean protoEnable = getConfig().getBoolean("skin.protocollib-enable", true);
        boolean refreshTablist = getConfig().getBoolean("skin.refresh-tablist", true);
        textureCache = new TextureCache(this, skinTtl);
        skinService = new SkinServiceImpl(this, textureCache, protoEnable, refreshTablist, preferencesManager);
        if (getConfig().getBoolean("skin.apply-on-login", true)) {
            getServer().getPluginManager().registerEvents(new SkinListener(skinService, preferencesManager), this);
        }

        PreferencesCommand prefsCmd = new PreferencesCommand(this, preferencesManager);
        DebugCommand debugCmd = new DebugCommand(this, sqliteManager, preferencesManager, skinService, textureCache);
        AuthCommand authCmd = new AuthCommand(this, claimCmd, prefsCmd, debugCmd);
        bind("heneria", authCmd, authCmd);
        getServer().getScheduler().runTaskTimerAsynchronously(this, claimManager::cleanupExpired, 20L, 20L * 60);
    }

    @Override
    public void onDisable() {
        if (authManager != null) {
            authManager.shutdown();
        }
        if (skinService != null) {
            skinService.shutdown();
        }
        getLogger().info("HeneriaCore stopped.");
    }

    public AuthManager getAuthManager() { return authManager; }

    public ClaimManager getClaimManager() { return claimManager; }

    public PreferencesManager getPreferencesManager() { return preferencesManager; }

    private void bind(String cmd, CommandExecutor exec, @Nullable TabCompleter tab) {
        PluginCommand c = getCommand(cmd);
        if (c == null) {
            getLogger().severe("Missing command in plugin.yml: " + cmd);
            return;
        }
        c.setExecutor(exec);
        if (tab != null) {
            c.setTabCompleter(tab);
        }
    }
}
