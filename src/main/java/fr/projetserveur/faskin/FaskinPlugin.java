package fr.projetserveur.faskin;

import org.bukkit.plugin.java.JavaPlugin;

public class FaskinPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("Faskin plugin enabled");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Faskin plugin disabled");
    }
}
