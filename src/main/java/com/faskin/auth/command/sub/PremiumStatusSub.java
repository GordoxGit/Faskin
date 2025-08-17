package com.faskin.auth.command.sub;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.tab.PremiumTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class PremiumStatusSub implements PremiumSub {
    private final FaskinPlugin plugin;

    public PremiumStatusSub(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(plugin.messages().prefixed("premium.error.no-target"));
                return;
            }
            if (!sender.hasPermission("faskin.premium.status")) {
                sender.sendMessage(plugin.messages().prefixed("premium.error.no-permission"));
                return;
            }
            PremiumTabCompleter.record(p.getName());
            String key = p.getName().toLowerCase(Locale.ROOT);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                AccountRepository repo = plugin.services().accounts();
                var info = repo.getPremiumInfo(key).orElse(null);
                var session = repo.getSessionMeta(key).orElse(null);
                boolean prem = info != null && info.isPremium;
                String mode = info != null && info.premiumMode != null ? info.premiumMode : "NONE";
                String uuid = info != null && info.uuidOnline != null ? info.uuidOnline : "n/a";
                String uuidShort = uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
                String verified = info != null && info.verifiedAtEpoch > 0
                        ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
                                .format(Instant.ofEpochSecond(info.verifiedAtEpoch))
                        : "never";
                String sessionState = "inactive";
                long ttl = 0L;
                if (plugin.configs().allowIpSession() && plugin.configs().sessionMinutes() > 0 && session != null) {
                    long now = Instant.now().getEpochSecond();
                    long diff = now - session.lastLoginEpoch;
                    long ttlSec = plugin.configs().sessionMinutes() * 60L - diff;
                    if (ttlSec > 0 && session.lastIp != null) {
                        sessionState = "active";
                        ttl = ttlSec;
                    }
                }
                long finalTtl = ttl;
                String finalSessionState = sessionState;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String prefix = plugin.messages().raw("prefix");
                    boolean usePrefix = plugin.getConfig().getBoolean("messages.use_prefix", true);
                    for (String line : plugin.messages().raw("premium.status-self").split("\\n")) {
                        line = line.replace("{is_premium}", String.valueOf(prem))
                                .replace("{mode}", mode)
                                .replace("{uuid_online}", uuidShort)
                                .replace("{verified_at}", verified)
                                .replace("{session}", finalSessionState)
                                .replace("{ttl}", String.valueOf(finalTtl));
                        if (usePrefix) line = prefix + line;
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                    }
                });
            });
        } else {
            if (!sender.hasPermission("faskin.premium.status.other")) {
                sender.sendMessage(plugin.messages().prefixed("premium.error.no-permission"));
                return;
            }
            String target = args[0];
            PremiumTabCompleter.record(target);
            String key = target.toLowerCase(Locale.ROOT);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                AccountRepository repo = plugin.services().accounts();
                var info = repo.getPremiumInfo(key).orElse(null);
                boolean prem = info != null && info.isPremium;
                String mode = info != null && info.premiumMode != null ? info.premiumMode : "NONE";
                String uuid = info != null && info.uuidOnline != null ? info.uuidOnline : "n/a";
                String uuidShort = uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String prefix = plugin.messages().raw("prefix");
                    boolean usePrefix = plugin.getConfig().getBoolean("messages.use_prefix", true);
                    for (String line : plugin.messages().raw("premium.status-other").split("\\n")) {
                        line = line.replace("{player}", target)
                                .replace("{is_premium}", String.valueOf(prem))
                                .replace("{mode}", mode)
                                .replace("{uuid_online}", uuidShort);
                        if (usePrefix) line = prefix + line;
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                    }
                });
            });
        }
    }
}
