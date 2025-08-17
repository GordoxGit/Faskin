package com.faskin.auth.command;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.command.sub.PremiumSub;
import com.faskin.auth.command.sub.PremiumStatusSub;
import com.faskin.auth.command.sub.PremiumUnlinkSub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class PremiumCommand implements CommandExecutor {
    private final FaskinPlugin plugin;
    private final Map<String, PremiumSub> subs = new HashMap<>();

    public PremiumCommand(FaskinPlugin plugin) {
        this.plugin = plugin;
        subs.put("status", new PremiumStatusSub(plugin));
        subs.put("unlink", new PremiumUnlinkSub(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("faskin.premium.base")) {
            sender.sendMessage(plugin.messages().prefixed("premium.error.no-permission"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(plugin.messages().prefixed("premium.error.no-target"));
            return true;
        }
        String subName = args[0].toLowerCase(Locale.ROOT);
        PremiumSub sub = subs.get(subName);
        if (sub == null) {
            sender.sendMessage(plugin.messages().prefixed("premium.error.no-target"));
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        sub.execute(sender, subArgs);
        return true;
    }
}
