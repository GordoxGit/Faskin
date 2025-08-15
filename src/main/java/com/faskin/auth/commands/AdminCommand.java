package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class AdminCommand implements CommandExecutor, TabCompleter {
    private final FaskinPlugin plugin;

    public AdminCommand(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sender.sendMessage(plugin.messages().prefixed("help_header"));
            for (String line : plugin.messages().raw("help_lines").split("\\n")) {
                sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            }
            sender.sendMessage(org.bukkit.ChatColor.GRAY + "Admin: /faskin reload | status [player] | unlock <player> | stats");
            return true;
        }
        if (!sender.hasPermission("faskin.admin")) {
            sender.sendMessage("[Faskin] Permission manquante.");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.messages().reload();
                sender.sendMessage(plugin.messages().prefixed("reloaded"));
                return true;
            }
            case "status" -> {
                String target = args.length >= 2 ? args[1] : (sender instanceof Player p ? p.getName() : null);
                if (target == null) { sender.sendMessage("[Faskin] /faskin status <player>"); return true; }
                String key = target.toLowerCase(Locale.ROOT);

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    AccountRepository repo = plugin.services().accounts();
                    var infoOpt = repo.adminInfo(key);
                    var info = infoOpt.orElse(new AccountRepository.AdminInfo(false, null, 0L, 0, 0L));

                    // Online state si connecté
                    Player online = Bukkit.getPlayerExact(target);
                    PlayerAuthState st = online != null ? plugin.services().getState(online.getUniqueId()) : PlayerAuthState.UNREGISTERED;

                    String last = (info.lastLoginEpoch > 0)
                            ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.ofEpochSecond(info.lastLoginEpoch))
                            : "never";
                    final long left;
                    if (info.lockedUntilEpoch > 0) {
                        long now = System.currentTimeMillis() / 1000L;
                        left = Math.max(0L, info.lockedUntilEpoch - now);
                    } else {
                        left = 0L;
                    }

                    String acc = info.exists ? "YES" : "NO";
                    String ip = info.lastIp != null ? info.lastIp : "n/a";
                    String lock = info.lockedUntilEpoch > 0 ? String.valueOf(info.lockedUntilEpoch) : "none";

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.messages().prefixed("admin_status_header").replace("{PLAYER}", target));
                        for (String line : plugin.messages().raw("admin_status_lines").split("\\n")) {
                            line = line.replace("{PLAYER}", target)
                                    .replace("{STATE}", st.name())
                                    .replace("{ACCOUNT}", acc)
                                    .replace("{IP}", ip)
                                    .replace("{LAST}", last)
                                    .replace("{FAILS}", String.valueOf(info.failedCount))
                                    .replace("{LOCK}", lock)
                                    .replace("{LOCKLEFT}", String.valueOf(left));
                            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
                        }
                    });
                });
                return true;
            }
            case "unlock" -> {
                if (args.length < 2) { sender.sendMessage("[Faskin] /faskin unlock <player>"); return true; }
                String target = args[1];
                String key = target.toLowerCase(Locale.ROOT);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    AccountRepository repo = plugin.services().accounts();
                    boolean existed = repo.exists(key);
                    if (existed) repo.resetFailures(key);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                                (existed ? plugin.messages().prefixed("admin_unlock_ok") : plugin.messages().prefixed("admin_unlock_noacc"))
                                        .replace("{PLAYER}", target)));
                    });
                });
                return true;
            }
            case "stats" -> {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    AccountRepository repo = plugin.services().accounts();
                    int accounts = repo.countAccounts();
                    int locked = repo.countLockedActive(System.currentTimeMillis() / 1000L);
                    var map = plugin.services().countStates();
                    int onlineAuth = map.getOrDefault(PlayerAuthState.AUTHENTICATED, 0);
                    int tmp = 0;
                    for (var e : map.entrySet())
                        if (e.getKey() != PlayerAuthState.AUTHENTICATED) tmp += e.getValue();
                    final int onlineNonAuth = tmp;

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(plugin.messages().prefixed("admin_stats_header"));
                        for (String line : plugin.messages().raw("admin_stats_lines").split("\\n")) {
                            line = line.replace("{ACCOUNTS}", String.valueOf(accounts))
                                    .replace("{LOCKED}", String.valueOf(locked))
                                    .replace("{ONLINE_AUTH}", String.valueOf(onlineAuth))
                                    .replace("{ONLINE_NONAUTH}", String.valueOf(onlineNonAuth));
                            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
                        }
                    });
                });
                return true;
            }
            default -> {
                sender.sendMessage("[Faskin] Usage: /faskin reload|status [player] | unlock <player> | stats | help");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String opt : new String[]{"help","reload","status","unlock","stats"}) {
                if (opt.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(opt);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("unlock"))) {
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        }
        return out;
    }
}
