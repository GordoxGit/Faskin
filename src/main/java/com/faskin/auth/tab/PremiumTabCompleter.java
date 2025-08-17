package com.faskin.auth.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class PremiumTabCompleter implements TabCompleter {
    private static final int MAX = 25;
    private static final LinkedHashSet<String> RECENTS = new LinkedHashSet<>();

    public static void record(String name) {
        synchronized (RECENTS) {
            RECENTS.remove(name);
            RECENTS.add(name);
            while (RECENTS.size() > MAX) {
                Iterator<String> it = RECENTS.iterator();
                if (it.hasNext()) { it.next(); it.remove(); }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String opt : new String[]{"status","unlink"}) {
                if (opt.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(opt);
            }
            return out;
        }
        if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
            synchronized (RECENTS) { out.addAll(RECENTS); }
            String pref = args[1].toLowerCase(Locale.ROOT);
            out.removeIf(n -> !n.toLowerCase(Locale.ROOT).startsWith(pref));
            return out;
        }
        return out;
    }
}
