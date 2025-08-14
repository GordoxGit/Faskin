package fr.tonpackage.heneriacore;

import org.bukkit.plugin.java.JavaPlugin;

public final class HeneriaCore extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HeneriaCore v0.0.1 enabled");
    }
    @Override
    public void onDisable() {
        getLogger().info("HeneriaCore disabled");
    }
}
