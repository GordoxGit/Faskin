package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RegisterCommand implements CommandExecutor {
    private final FaskinPlugin plugin;

    public RegisterCommand(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("[Faskin] In-game uniquement."); return true; }
        if (args.length != 2) { p.sendMessage("[Faskin] Usage: /register <password> <confirm>"); return true; }
        String pass = args[0]; String confirm = args[1];
        if (!pass.equals(confirm)) { p.sendMessage("[Faskin] Les mots de passe ne correspondent pas."); return true; }
        if (pass.length() < plugin.configs().passwordMinLength()) { p.sendMessage("[Faskin] Mot de passe trop court."); return true; }
        boolean hasDigit = pass.chars().anyMatch(Character::isDigit);
        boolean hasLetter = pass.chars().anyMatch(Character::isLetter);
        if (plugin.configs().requireDigit() && !hasDigit) { p.sendMessage("[Faskin] Le mot de passe doit contenir un chiffre."); return true; }
        if (plugin.configs().requireLetter() && !hasLetter) { p.sendMessage("[Faskin] Le mot de passe doit contenir une lettre."); return true; }

        AccountRepository repo = plugin.services().accounts();
        String key = p.getName().toLowerCase();
        if (repo.exists(key)) { p.sendMessage("[Faskin] Compte déjà enregistré. Faites /login <mdp>."); return true; }

        var hasher = new com.faskin.auth.security.Pbkdf2Hasher();
        byte[] salt = hasher.newSalt();
        byte[] hash = hasher.hash(pass.toCharArray(), salt);
        repo.create(key, salt, hash);

        plugin.services().setState(p.getUniqueId(), PlayerAuthState.REGISTERED_UNAUTH);
        p.sendMessage(plugin.messages().raw("registered_ok"));
        p.sendMessage(plugin.messages().raw("must_login"));
        return true;
    }
}
