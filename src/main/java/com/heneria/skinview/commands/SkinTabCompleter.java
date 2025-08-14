package com.heneria.skinview.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class SkinTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("help");
            out.add("use");
            out.add("url");
            out.add("clear");
            if (sender.hasPermission("skinview.admin")) {
                out.add("reload");
                out.add("resolve");
                out.add("cache");
                out.add("debug");
            }
            out.add("optin");
            out.add("optout");
        } else if (args.length == 2 && "resolve".equalsIgnoreCase(args[0]) && sender.hasPermission("skinview.admin")) {
            out.add("name"); out.add("url");
        } else if (args.length == 2 && "cache".equalsIgnoreCase(args[0]) && sender.hasPermission("skinview.admin")) {
            out.add("get"); out.add("clear");
        } else if (args.length == 2 && ("optin".equalsIgnoreCase(args[0]) || "optout".equalsIgnoreCase(args[0])) && sender.hasPermission("skinview.admin")) {
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        }
        return out;
    }
}

