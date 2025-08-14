package com.heneria.skinview.commands;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinDescriptor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
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

            CompletableFuture<SkinDescriptor> fut;
            if ("name".equals(type)) fut = plugin.resolver().resolveByPremiumName(value);
            else if ("url".equals(type)) fut = plugin.resolver().resolveByTexturesUrl(value);
            else { sendPrefixed(sender, "resolve-usage", "&eUsage: /skinview resolve <name|url> <value>"); return true; }

            fut.whenComplete((sd, ex) -> Bukkit.getScheduler().runTask(plugin, () -> {
                if (ex != null) {
                    sendPrefixed(sender, "resolve-fail", "&cÉchec de résolution: " + ex.getMessage());
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.messages().getString("prefix", "") +
                                    "&aOK&7 → &f" + sd.skinUrl() + " &7(" + sd.model() + ")"));
                }
            }));
            return true;
        }

        sendPrefixed(sender, "unknown-subcommand", "&cUnknown subcommand.");
        return true;
    }
}
