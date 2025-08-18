package com.monprojet.faskin;

import com.monprojet.faskin.database.MySQLManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Faskin extends JavaPlugin {

    private MySQLManager mySQLManager;

    @Override
    public void onEnable() {
        // Créer le fichier config.yml par défaut s'il n'existe pas
        saveDefaultConfig();

        // Initialiser et connecter à la base de données
        this.mySQLManager = new MySQLManager(this);
        this.mySQLManager.connect();

        getLogger().info("Faskin a été activé.");
    }

    @Override
    public void onDisable() {
        if (this.mySQLManager != null) {
            this.mySQLManager.disconnect();
        }
        getLogger().info("Faskin a été désactivé.");
    }
}
