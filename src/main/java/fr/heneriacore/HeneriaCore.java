package fr.heneriacore;

import org.bukkit.plugin.java.JavaPlugin;

public final class HeneriaCore extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HeneriaCore v0.0.1 starting...");
        // TODO: register services / listeners here (HC-02+)
    }

    @Override
    public void onDisable() {
        getLogger().info("HeneriaCore stopped.");
    }
}
