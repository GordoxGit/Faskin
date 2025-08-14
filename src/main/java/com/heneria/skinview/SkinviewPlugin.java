package com.heneria.skinview;

import com.heneria.skinview.commands.SkinCommand;
import com.heneria.skinview.commands.SkinTabCompleter;
import com.heneria.skinview.listener.InteractListener;
import com.heneria.skinview.listener.JoinListener;
import com.heneria.skinview.listener.SkinAutoApplyJoinListener;
import com.heneria.skinview.service.SkinApplier;
import com.heneria.skinview.service.SkinResolver;
import com.heneria.skinview.service.SkinService;
import com.heneria.skinview.service.impl.MojangSkinResolver;
import com.heneria.skinview.service.impl.PaperSkinApplier;
import com.heneria.skinview.store.SkinStore;
import com.heneria.skinview.store.YamlSkinStore;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public final class SkinviewPlugin extends JavaPlugin {

    private FileConfiguration messages;
    private SkinResolver resolver;
    private SkinService skinService;
    private SkinApplier applier;
    private SkinStore store;

    @Override
    public void onEnable() {
        final long t0 = System.nanoTime();

        saveDefaultConfig();
        saveResource("messages.yml", false);
        reloadMessages();

        final PluginCommand cmd = getCommand("skinview");
        if (cmd == null) {
            getLogger().severe("Commande /skinview introuvable (plugin.yml non packagé ?). Désactivation.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        cmd.setExecutor(new SkinCommand(this));
        cmd.setTabCompleter(new SkinTabCompleter());

        final PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinListener(this), this);
        pm.registerEvents(new InteractListener(this), this);
        pm.registerEvents(new SkinAutoApplyJoinListener(this), this);

        this.resolver = new MojangSkinResolver(this);
        this.applier = chooseApplier();
        this.store = new YamlSkinStore(this);
        this.skinService = new SkinService(this, resolver, applier, store);

        final long dtMs = (System.nanoTime() - t0) / 1_000_000;
        getLogger().info(String.format(
            "skinview v%s enabled in %d ms (Java %s, API %s)",
            getDescription().getVersion(), dtMs, System.getProperty("java.version"), getServer().getBukkitVersion()
        ));
    }

    @Override
    public void onDisable() {
        if (resolver != null) { resolver.shutdown(); resolver = null; }
        if (applier != null) {
            try {
                if (applier.getClass().getName().endsWith("SkinApplierProtocolLib")) {
                    applier.getClass().getMethod("shutdown").invoke(applier);
                }
            } catch (Exception ignored) {}
            applier = null;
        }
        getLogger().info("skinview disabled.");
    }

    private SkinApplier chooseApplier() {
        boolean enablePlib = getConfig().getBoolean("apply.protocollib-enable", true);
        if (enablePlib && getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                Class<?> c = Class.forName("com.heneria.skinview.service.impl.SkinApplierProtocolLib");
                getLogger().info("[skinview] ProtocolLib applier actif.");
                return (SkinApplier) c.getConstructor(SkinviewPlugin.class).newInstance(this);
            } catch (ClassNotFoundException ignored) {
                getLogger().info("[skinview] Classe PLib absente (build sans -PwithPlib). Fallback.");
            } catch (Exception e) {
                getLogger().warning("[skinview] Echec init ProtocolLib applier: " + e.getMessage());
            }
        }
        try {
            org.bukkit.entity.Player.class.getMethod("setPlayerProfile", org.bukkit.profile.PlayerProfile.class);
            getLogger().info("[skinview] Applier Paper (reflection).");
            return new PaperSkinApplier(this);
        } catch (NoSuchMethodException ignored) {
            getLogger().info("[skinview] Spigot sans ProtocolLib: apply live non disponible (fallback).");
            return new SkinApplier() {
                @Override public void apply(org.bukkit.entity.Player p, com.heneria.skinview.service.SkinDescriptor d) {}
                @Override public void clear(org.bukkit.entity.Player p) {}
            };
        }
    }

    public FileConfiguration messages() { return this.messages; }

    public void reloadAll() {
        reloadConfig();
        reloadMessages();
        if (resolver != null) resolver.reloadSettings();
        getLogger().info("Configuration & messages rechargés.");
    }

    private void reloadMessages() {
        try {
            File file = new File(getDataFolder(), "messages.yml");
            this.messages = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Chargement messages.yml : " + e.getMessage(), e);
            this.messages = getConfig();
        }
    }

    public List<String> helpLines() {
        List<String> lines = messages().getStringList("help");
        final String ver = getDescription().getVersion();
        for (int i = 0; i < lines.size(); i++) lines.set(i, lines.get(i).replace("%version%", ver));
        return lines;
    }

    public SkinResolver resolver() { return resolver; }
    public SkinService skinService() { return skinService; }
}

