package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class AdminCommand implements CommandExecutor, TabCompleter {
    private final FaskinPlugin plugin;

    public AdminCommand(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("faskin.admin")) {
            sender.sendMessage("[Faskin] Permission manquante.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("[Faskin] Usage: /faskin reload|status [player]");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.messages().reload();
                sender.sendMessage("[Faskin] Configuration rechargée.");
                return true;
            }
            case "status" -> {
                if (args.length < 2) {
                    sender.sendMessage("[Faskin] /faskin status <player>");
                    return true;
                }
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    sender.sendMessage("[Faskin] Joueur hors ligne.");
                    return true;
                }
                PlayerAuthState st = plugin.services().getState(p.getUniqueId());
                sender.sendMessage("[Faskin] " + p.getName() + " => " + st);
                return true;
            }
            default -> {
                sender.sendMessage("[Faskin] Usage: /faskin reload|status [player]");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) out.add("reload");
            if ("status".startsWith(args[0].toLowerCase())) out.add("status");
        } else if (args.length == 2 && "status".equalsIgnoreCase(args[0])) {
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        }
        return out;
    }
}
