package com.heneria.skinview.commands;

import com.heneria.skinview.SkinviewPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class SkinCommand implements CommandExecutor {

    private final SkinviewPlugin plugin;

    public SkinCommand(SkinviewPlugin plugin) {
        this.plugin = plugin;
    }

    private void sendPrefixed(CommandSender sender, String path, String def) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.messages().getString("prefix", ""));
        String raw = plugin.messages().getString(path, def);
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', raw));
    }

    private void sendHelp(CommandSender sender) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.messages().getString("prefix", ""));
        for (String raw : plugin.helpLines()) {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', raw));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sendHelp(sender);
            return true; // jamais false (évite l'"usage"/écho)
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("skinview.admin")) {
                sendPrefixed(sender, "no-permission", "&cNo permission.");
                return true;
            }
            plugin.reloadAll();
            sendPrefixed(sender, "reloaded", "&aReloaded.");
            return true;
        }

        sendPrefixed(sender, "unknown-subcommand", "&cUnknown subcommand.");
        return true;
    }
}
