package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.net.InetSocketAddress;
import java.time.Instant;

public final class LoginCommand implements CommandExecutor {
    private final FaskinPlugin plugin;
    public LoginCommand(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player p)) { sender.sendMessage("[Faskin] In-game uniquement."); return true; }
        if (args.length != 1) { p.sendMessage("[Faskin] Usage: /login <password>"); return true; }

        String pass = args[0];
        String key = p.getName().toLowerCase();
        InetSocketAddress sock = p.getAddress();
        String ip = (sock != null && sock.getAddress() != null) ? sock.getAddress().getHostAddress() : "unknown";
        long now = Instant.now().getEpochSecond();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AccountRepository repo = plugin.services().accounts();

            var opt = repo.find(key);
            if (opt.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(plugin.messages().prefixed("must_register")));
                return;
            }
            var acc = opt.get();
            var hasher = new com.faskin.auth.security.Pbkdf2Hasher();
            boolean ok = hasher.verify(pass.toCharArray(), acc.salt, acc.hash);

            if (!ok) {
                Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(plugin.messages().prefixed("bad_credentials")));
                return;
            }

            // Succès: mise à jour session + état
            repo.updateLastLoginAndIp(key, ip, now);
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.services().setState(p.getUniqueId(), PlayerAuthState.AUTHENTICATED);
                p.sendMessage(plugin.messages().prefixed("login_ok"));
            });
        });
        return true;
    }
}

