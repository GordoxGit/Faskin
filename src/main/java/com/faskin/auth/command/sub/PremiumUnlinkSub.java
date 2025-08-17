package com.faskin.auth.command.sub;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import com.faskin.auth.tab.PremiumTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class PremiumUnlinkSub implements PremiumSub {
    private final FaskinPlugin plugin;

    public PremiumUnlinkSub(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(plugin.messages().prefixed("premium.error.no-target"));
                return;
            }
            if (!sender.hasPermission("faskin.premium.unlink.self")) {
                sender.sendMessage(plugin.messages().prefixed("premium.error.no-permission"));
                return;
            }
            PremiumTabCompleter.record(p.getName());
            String key = p.getName().toLowerCase(Locale.ROOT);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                AccountRepository repo = plugin.services().accounts();
                var info = repo.getPremiumInfo(key).orElse(null);
                if (info == null || !info.isPremium) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(plugin.messages().prefixed("premium.unlink.not-premium")));
                    return;
                }
                repo.updatePremiumInfo(key, false, null, null, 0L);
                repo.updateLastLoginAndIp(key, null, 0L);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.services().setState(p.getUniqueId(), PlayerAuthState.REGISTERED_UNAUTH);
                    p.sendMessage(plugin.messages().prefixed("premium.unlink.self-ok"));
                });
            });
        } else {
            if (!sender.hasPermission("faskin.premium.unlink.other")) {
                sender.sendMessage(plugin.messages().prefixed("premium.error.no-permission"));
                return;
            }
            String target = args[0];
            PremiumTabCompleter.record(target);
            String key = target.toLowerCase(Locale.ROOT);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                AccountRepository repo = plugin.services().accounts();
                var info = repo.getPremiumInfo(key).orElse(null);
                if (info == null || !info.isPremium) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(plugin.messages().prefixed("premium.unlink.not-premium")));
                    return;
                }
                repo.updatePremiumInfo(key, false, null, null, 0L);
                repo.updateLastLoginAndIp(key, null, 0L);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player online = Bukkit.getPlayerExact(target);
                    if (online != null) {
                        plugin.services().setState(online.getUniqueId(), PlayerAuthState.REGISTERED_UNAUTH);
                    }
                    plugin.getLogger().info("[Faskin/Premium] unlink by " + sender.getName() + " target=" + target);
                    sender.sendMessage(plugin.messages().prefixed("premium.unlink.other-ok").replace("{player}", target));
                });
            });
        }
    }
}
