package com.heneria.skinview.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class SkinTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("help");
            if (sender.hasPermission("skinview.admin")) {
                out.add("reload"); out.add("resolve");
            }
        } else if (args.length == 2 && "resolve".equalsIgnoreCase(args[0]) && sender.hasPermission("skinview.admin")) {
            out.add("name"); out.add("url");
        }
        return out;
    }
}
