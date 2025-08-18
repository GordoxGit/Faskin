package com.monprojet.faskin.commands;

import at.favre.lib.bcrypt.BCrypt;
import com.monprojet.faskin.Faskin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final Faskin plugin;

    public RegisterCommand(Faskin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;

        // Vérification de la syntaxe de la commande
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Syntaxe : /register <motdepasse> <confirmation>");
            return true;
        }

        // Vérifier si le joueur est déjà enregistré
        if (plugin.getMySQLManager().isPlayerRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Vous êtes déjà enregistré sur le serveur.");
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        // Vérifier si les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            player.sendMessage(ChatColor.RED + "Les mots de passe ne correspondent pas.");
            return true;
        }

        // Vérifier la robustesse du mot de passe
        if (password.length() < 8) {
            player.sendMessage(ChatColor.RED + "Votre mot de passe doit contenir au moins 8 caractères.");
            return true;
        }

        // Hacher le mot de passe
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        // Enregistrer le joueur
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        plugin.getMySQLManager().registerPlayer(player.getUniqueId(), player.getName(), hashedPassword, ipAddress);

        player.sendMessage(ChatColor.GREEN + "Vous avez été enregistré avec succès ! Vous pouvez maintenant vous connecter avec /login.");
        // Ici, on pourrait ajouter une logique pour connecter automatiquement le joueur.

        return true;
    }
}
