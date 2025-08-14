package com.heneria.skinview.commands;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SkinCommand implements CommandExecutor {

    private final SkinviewPlugin plugin;

    public SkinCommand(SkinviewPlugin plugin) { this.plugin = plugin; }

    private void sendPrefixed(CommandSender sender, String path, String def) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.messages().getString("prefix", ""));
        String raw = plugin.messages().getString(path, def);
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', raw));
    }

    private void sendHelp(CommandSender sender) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.messages().getString("prefix", ""));
        for (String raw : plugin.helpLines()) sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', raw));
    }

    private Player resolveTarget(CommandSender sender, String[] args, int index) {
        if (args.length <= index) {
            if (sender instanceof Player p) return p;
            return null;
        }
        return Bukkit.getPlayerExact(args[index]);
    }

    private boolean canTargetOthers(CommandSender sender) {
        return sender.hasPermission("skinview.admin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sendHelp(sender);
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("skinview.admin")) { sendPrefixed(sender, "no-permission", "&cNo permission."); return true; }
            plugin.reloadAll();
            sendPrefixed(sender, "reloaded", "&aReloaded.");
            return true;
        }

        if ("resolve".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("skinview.admin")) { sendPrefixed(sender, "no-permission", "&cNo permission."); return true; }
            if (args.length < 3) { sendPrefixed(sender, "resolve-usage", "&eUsage: /skinview resolve <name|url> <value>"); return true; }
            String type = args[1].toLowerCase();
            String value = args[2];
            sendPrefixed(sender, "resolve-started", "&7Résolution en cours...");
            var fut = "name".equals(type)
                    ? plugin.resolver().resolveByPremiumName(value)
                    : "url".equals(type) ? plugin.resolver().resolveByTexturesUrl(value)
                    : null;
            if (fut == null) { sendPrefixed(sender, "resolve-usage", "&eUsage: /skinview resolve <name|url> <value>"); return true; }
            fut.whenComplete((sd, ex) -> Bukkit.getScheduler().runTask(plugin, () -> {
                if (ex != null) sendPrefixed(sender, "resolve-fail", "&cÉchec: %error%".replace("%error%", ex.getMessage()));
                else sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.messages().getString("prefix", "") + "&aOK&7 (voir console pour détails éventuels)"));
            }));
            return true;
        }

        if ("use".equalsIgnoreCase(args[0]) || "url".equalsIgnoreCase(args[0])) {
            boolean byName = "use".equalsIgnoreCase(args[0]);
            if (args.length < 2) { sendPrefixed(sender, "invalid-arg", "&cArguments invalides."); return true; }
            Player target = resolveTarget(sender, args, 2);
            if (target == null) {
                if (args.length >= 3 && !(sender instanceof Player)) {
                    sendPrefixed(sender, "player-not-found", "&cJoueur introuvable: %player%".replace("%player%", args[2]));
                    return true;
                }
                if (!(sender instanceof Player)) { sendPrefixed(sender, "invalid-arg", "&cSpécifiez un joueur."); return true; }
            }
            if (target != null && !((sender instanceof Player p && p.equals(target)) || canTargetOthers(sender))) {
                sendPrefixed(sender, "no-permission", "&cNo permission."); return true;
            }
            SkinService svc = plugin.skinService();
            if (byName) svc.applyByPremiumName(sender, target != null ? target : (Player) sender, args[1]);
            else svc.applyByTexturesUrl(sender, target != null ? target : (Player) sender, args[1]);
            return true;
        }

        if ("clear".equalsIgnoreCase(args[0])) {
            Player target = resolveTarget(sender, args, 1);
            if (target == null) {
                if (args.length >= 2) sendPrefixed(sender, "player-not-found", "&cJoueur introuvable: %player%".replace("%player%", args[1]));
                else if (!(sender instanceof Player)) sendPrefixed(sender, "invalid-arg", "&cSpécifiez un joueur.");
                return true;
            }
            if (!((sender instanceof Player p && p.equals(target)) || canTargetOthers(sender))) {
                sendPrefixed(sender, "no-permission", "&cNo permission."); return true;
            }
            plugin.skinService().clearCache(sender, target);
            return true;
        }

        if ("cache".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("skinview.admin")) { sendPrefixed(sender, "no-permission", "&cNo permission."); return true; }
            if (args.length < 2) { sendPrefixed(sender, "cache-usage", "&eUsage: /skinview cache <get|clear> [joueur]"); return true; }
            String sub = args[1].toLowerCase();
            if ("get".equals(sub)) {
                Player target = resolveTarget(sender, args, 2);
                if (target == null) { sendPrefixed(sender, "player-not-found", "&cJoueur introuvable: %player%".replace("%player%", args.length > 2 ? args[2] : "?")); return true; }
                boolean ok = plugin.skinService().applyFromStore(sender, target);
                if (!ok) sendPrefixed(sender, "cache-miss", "&eAucune entrée de cache valide.");
                return true;
            } else if ("clear".equals(sub)) {
                Player target = resolveTarget(sender, args, 2);
                if (target == null) { sendPrefixed(sender, "player-not-found", "&cJoueur introuvable: %player%".replace("%player%", args.length > 2 ? args[2] : "?")); return true; }
                plugin.skinService().clearCache(sender, target);
                return true;
            } else {
                sendPrefixed(sender, "cache-usage", "&eUsage: /skinview cache <get|clear> [joueur]");
                return true;
            }
        }

        if ("optout".equalsIgnoreCase(args[0]) || "optin".equalsIgnoreCase(args[0])) {
            boolean opt = "optout".equalsIgnoreCase(args[0]);
            Player target = resolveTarget(sender, args, 1);
            if (target == null) {
                if (args.length >= 2) sendPrefixed(sender, "player-not-found", "&cJoueur introuvable: %player%".replace("%player%", args[1]));
                else if (!(sender instanceof Player)) sendPrefixed(sender, "invalid-arg", "&cSpécifiez un joueur.");
                return true;
            }
            if (!((sender instanceof Player p && p.equals(target)) || canTargetOthers(sender))) {
                sendPrefixed(sender, "no-permission", "&cNo permission.");
                return true;
            }
            plugin.flagStore().setOptOut(target.getUniqueId(), opt).whenComplete((v, ex) -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (ex != null) {
                        sendPrefixed(sender, "opt-fail", "&cÉchec mise à jour: %error%".replace("%error%", ex.getMessage()));
                    } else {
                        String key = opt ? "optout-ok" : "optin-ok";
                        String def = opt ? "&aAuto-apply désactivé pour &f%player%&a." : "&aAuto-apply activé pour &f%player%&a.";
                        sendPrefixed(sender, key, def.replace("%player%", target.getName()));
                    }
                });
            });
            return true;
        }

        if ("debug".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("skinview.admin")) { sendPrefixed(sender, "no-permission", "&cNo permission."); return true; }
            var dbg = plugin.debug();
            int cacheSize = dbg.resolverCacheSize();
            long cacheTtl = dbg.resolverTtlSeconds();
            int storeEntries = dbg.storeEntries();
            long storeSize = dbg.storeFileSize();
            long storeTtl = dbg.storeTtlSeconds();
            int optouts = dbg.optOutCount();
            long hits = dbg.mojangHits();
            long suc = dbg.mojangSuccesses();
            long fail = dbg.mojangFailures();
            long retry = dbg.mojangRetries();
            long thr = dbg.mojangThrottled();
            String circuit = dbg.circuitState().name();
            long lastFail = dbg.lastFailureTime();
            long up = dbg.uptimeSeconds();
            String ver = dbg.version();
            if (sender instanceof Player p) {
                var a = plugin.adventure().player(p);
                a.sendMessage(Component.text("=== skinview debug ===", NamedTextColor.GOLD));
                a.sendMessage(Component.text("Applier: ", NamedTextColor.YELLOW)
                        .append(Component.text(dbg.applierName(), NamedTextColor.WHITE)));
                a.sendMessage(Component.text("Cache: ", NamedTextColor.YELLOW)
                        .append(Component.text(cacheSize + " entries, ttl=" + cacheTtl + "s", NamedTextColor.WHITE)));
                a.sendMessage(Component.text("Store: ", NamedTextColor.YELLOW)
                        .append(Component.text(storeEntries + " entries, ttl=" + storeTtl + "s, file=" + storeSize + "B", NamedTextColor.WHITE)));
                a.sendMessage(Component.text("Opt-outs: ", NamedTextColor.YELLOW)
                        .append(Component.text(String.valueOf(optouts), NamedTextColor.WHITE)));
                a.sendMessage(Component.text("Mojang: ", NamedTextColor.YELLOW)
                        .append(Component.text("hits=" + hits + " ok=" + suc + " fail=" + fail + " retry=" + retry + " thr=" + thr + " circuit=" + circuit + " lastFail=" + lastFail, NamedTextColor.WHITE)));
                a.sendMessage(Component.text("Version: ", NamedTextColor.YELLOW)
                        .append(Component.text(ver + ", uptime=" + up + "s", NamedTextColor.WHITE)));
            } else {
                sender.sendMessage("=== skinview debug ===");
                sender.sendMessage("applier=" + dbg.applierName());
                sender.sendMessage("cache=" + cacheSize + " entries ttl=" + cacheTtl + "s");
                sender.sendMessage("store=" + storeEntries + " entries ttl=" + storeTtl + "s file=" + storeSize + "B");
                sender.sendMessage("opt-outs=" + optouts);
                sender.sendMessage("mojang hits=" + hits + " ok=" + suc + " fail=" + fail + " retry=" + retry + " thr=" + thr + " circuit=" + circuit + " lastFail=" + lastFail);
                sender.sendMessage("version=" + ver + " uptime=" + up + "s");
            }
            return true;
        }

        sendPrefixed(sender, "unknown-subcommand", "&cUnknown subcommand.");
        return true;
    }
}

