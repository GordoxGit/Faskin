package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import com.faskin.auth.util.AttemptThrottle;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.net.InetSocketAddress;
import java.time.Instant;

public final class LoginCommand implements CommandExecutor {
    private final FaskinPlugin plugin;
    private final AttemptThrottle throttle = new AttemptThrottle();

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

        // Cooldown local côté joueur
        long minSec = plugin.configs().minSecondsBetweenAttempts();
        if (!throttle.canAttempt(p.getUniqueId(), minSec)) {
            long left = throttle.secondsLeft(p.getUniqueId(), minSec);
            p.sendMessage(plugin.messages().prefixed("too_fast").replace("{SEC}", String.valueOf(left)));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AccountRepository repo = plugin.services().accounts();

            // Lock DB ?
            if (repo.isLocked(key)) {
                long left = repo.lockRemainingSeconds(key);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (left > 0) {
                        long min = left / 60; long sec = left % 60;
                        p.sendMessage(plugin.messages().prefixed("locked_with_time")
                                .replace("{MIN}", String.valueOf(min))
                                .replace("{SEC}", String.valueOf(sec)));
                    } else {
                        p.sendMessage(plugin.messages().prefixed("locked_generic"));
                    }
                });
                return;
            }

            var opt = repo.find(key);
            if (opt.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(plugin.messages().prefixed("must_register")));
                return;
            }
            var acc = opt.get();
            var hasher = new com.faskin.auth.security.Pbkdf2Hasher();
            boolean ok = hasher.verify(pass.toCharArray(), acc.salt, acc.hash);

            if (!ok) {
                // Enregistrer l'échec + lock éventuel
                int max = plugin.configs().maxFailedAttempts();
                long lockSec = plugin.configs().lockMinutes() * 60L;
                repo.registerFailedAttempt(key, max, lockSec);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (repo.isLocked(key)) {
                        long left = repo.lockRemainingSeconds(key);
                        long min = left / 60; long sec = left % 60;
                        p.sendMessage(plugin.messages().prefixed("locked_with_time")
                                .replace("{MIN}", String.valueOf(min))
                                .replace("{SEC}", String.valueOf(sec)));
                    } else {
                        p.sendMessage(plugin.messages().prefixed("bad_credentials"));
                    }
                });
                return;
            }

            // Succès: reset compteur + mise à jour session
            repo.resetFailures(key);
            repo.updateLastLoginAndIp(key, ip, now);

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.services().setState(p.getUniqueId(), PlayerAuthState.AUTHENTICATED);
                // Annule le timeout si présent
                plugin.getTimeouts().cancel(p.getUniqueId());
                p.sendMessage(plugin.messages().prefixed("login_ok"));
            });
        });
        return true;
    }
}
