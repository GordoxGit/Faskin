package com.monprojet.faskin;

import com.monprojet.faskin.commands.RegisterCommand;
import com.monprojet.faskin.database.MySQLManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Faskin extends JavaPlugin {

    private MySQLManager mySQLManager;

    @Override
    public void onEnable() {
        // Créer le fichier config.yml par défaut s'il n'existe pas
        saveDefaultConfig();

        this.mySQLManager = new MySQLManager(this);
        this.mySQLManager.connect();

        // Enregistrer la commande /register
        getCommand("register").setExecutor(new RegisterCommand(this));

        getLogger().info("Faskin a été activé.");
    }

    @Override
    public void onDisable() {
        if (this.mySQLManager != null) {
            this.mySQLManager.disconnect();
        }
        getLogger().info("Faskin a été désactivé.");
    }

    // Méthode pour accéder au MySQLManager depuis d'autres classes
    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }
}
