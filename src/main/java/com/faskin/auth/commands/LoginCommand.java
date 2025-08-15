package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.command.*;

public final class LoginCommand implements CommandExecutor {
    private final FaskinPlugin plugin;

    public LoginCommand(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player p)) { sender.sendMessage("[Faskin] In-game uniquement."); return true; }
        if (args.length != 1) { p.sendMessage("[Faskin] Usage: /login <password>"); return true; }
        String pass = args[0];
        AccountRepository repo = plugin.services().accounts();
        var opt = repo.find(p.getName().toLowerCase());
        if (opt.isEmpty()) { p.sendMessage(plugin.messages().raw("must_register")); return true; }

        var acc = opt.get();
        var hasher = new com.faskin.auth.security.Pbkdf2Hasher();
        boolean ok = hasher.verify(pass.toCharArray(), acc.salt, acc.hash);
        if (!ok) { p.sendMessage(plugin.messages().raw("bad_credentials")); return true; }

        plugin.services().setState(p.getUniqueId(), PlayerAuthState.AUTHENTICATED);
        p.sendMessage(plugin.messages().raw("login_ok"));
        return true;
    }
}
