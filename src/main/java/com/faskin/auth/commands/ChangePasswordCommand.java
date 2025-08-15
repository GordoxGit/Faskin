package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.command.*;

public final class ChangePasswordCommand implements CommandExecutor {
    private final FaskinPlugin plugin;

    public ChangePasswordCommand(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player p)) { sender.sendMessage("[Faskin] In-game uniquement."); return true; }
        if (args.length != 3) { p.sendMessage("[Faskin] Usage: /changepassword <old> <new> <confirm>"); return true; }
        if (plugin.services().getState(p.getUniqueId()) != PlayerAuthState.AUTHENTICATED) {
            p.sendMessage("[Faskin] Connectez-vous avant de changer le mot de passe.");
            return true;
        }
        String oldPass = args[0], newPass = args[1], confirm = args[2];
        if (!newPass.equals(confirm)) { p.sendMessage("[Faskin] Confirmation invalide."); return true; }
        if (newPass.length() < plugin.configs().passwordMinLength()) { p.sendMessage("[Faskin] Mot de passe trop court."); return true; }

        AccountRepository repo = plugin.services().accounts();
        var accOpt = repo.find(p.getName().toLowerCase());
        if (accOpt.isEmpty()) { p.sendMessage("[Faskin] Compte introuvable."); return true; }

        var acc = accOpt.get();
        var hasher = new com.faskin.auth.security.Pbkdf2Hasher();
        if (!hasher.verify(oldPass.toCharArray(), acc.salt, acc.hash)) {
            p.sendMessage("[Faskin] Ancien mot de passe incorrect.");
            return true;
        }
        byte[] salt = hasher.newSalt();
        byte[] hash = hasher.hash(newPass.toCharArray(), salt);
        repo.updatePassword(acc.usernameLower, salt, hash);
        p.sendMessage("[Faskin] Mot de passe mis à jour.");
        return true;
    }
}
