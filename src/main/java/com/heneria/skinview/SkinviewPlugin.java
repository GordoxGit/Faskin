package com.heneria.skinview;

import com.heneria.skinview.commands.SkinCommand;
import com.heneria.skinview.commands.SkinTabCompleter;
import com.heneria.skinview.listener.InteractListener;
import com.heneria.skinview.listener.JoinListener;
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

    @Override
    public void onEnable() {
        final long t0 = System.nanoTime();

        // Configs par défaut
        saveDefaultConfig();
        saveResource("messages.yml", false);
        reloadMessages();

        // Commande principale
        final PluginCommand cmd = getCommand("skinview");
        if (cmd == null) {
            getLogger().severe("Commande /skinview introuvable (plugin.yml non packagé ?). Désactivation.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        cmd.setExecutor(new SkinCommand(this));
        cmd.setTabCompleter(new SkinTabCompleter());

        // Listeners
        final PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinListener(this), this);
        pm.registerEvents(new InteractListener(this), this);

        final long dtMs = (System.nanoTime() - t0) / 1_000_000;
        getLogger().info(String.format(
            "skinview v%s enabled in %d ms (Java %s, API %s)",
            getDescription().getVersion(), dtMs, System.getProperty("java.version"), getServer().getBukkitVersion()
        ));
    }

    @Override
    public void onDisable() {
        getLogger().info("skinview disabled.");
    }

    public FileConfiguration messages() {
        return this.messages;
    }

    public void reloadAll() {
        reloadConfig();
        reloadMessages();
        getLogger().info("Configuration & messages rechargés.");
    }

    private void reloadMessages() {
        try {
            File file = new File(getDataFolder(), "messages.yml");
            this.messages = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Chargement messages.yml : " + e.getMessage(), e);
            this.messages = getConfig(); // fallback minimal
        }
    }

    public List<String> helpLines() {
        List<String> lines = messages().getStringList("help");
        final String ver = getDescription().getVersion();
        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, lines.get(i).replace("%version%", ver));
        }
        return lines;
    }
}
