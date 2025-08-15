package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
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

        String key = p.getName().toLowerCase();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AccountRepository repo = plugin.services().accounts();
            if (repo.exists(key)) {
                Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(plugin.messages().prefixed("must_login")));
                return;
            }
            var hasher = new com.faskin.auth.security.Pbkdf2Hasher();
            byte[] salt = hasher.newSalt();
            byte[] hash = hasher.hash(pass.toCharArray(), salt);
            repo.create(key, salt, hash);

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.services().setState(p.getUniqueId(), PlayerAuthState.REGISTERED_UNAUTH);
                p.sendMessage(plugin.messages().prefixed("registered_ok"));
                p.sendMessage(plugin.messages().prefixed("must_login"));
            });
        });
        return true;
    }
}
