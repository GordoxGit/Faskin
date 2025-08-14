package com.heneria.skinview.commands;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

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

        sendPrefixed(sender, "unknown-subcommand", "&cUnknown subcommand.");
        return true;
    }
}

