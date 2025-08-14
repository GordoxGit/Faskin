package fr.heneriacore.cmd;

import fr.heneriacore.HeneriaCore;
import fr.heneriacore.prefs.PreferencesManager;
import fr.heneriacore.prefs.PreferencesManager.PlayerPrefs;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PreferencesCommand implements TabCompleter {
    private final HeneriaCore plugin;
    private final PreferencesManager prefs;

    public PreferencesCommand(HeneriaCore plugin, PreferencesManager prefs) {
        this.plugin = plugin;
        this.prefs = prefs;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only");
            return true;
        }
        String sub = args[0].toLowerCase();
        UUID uuid = player.getUniqueId();
        switch (sub) {
            case "optout" -> {
                prefs.setOptOut(uuid, true).thenRun(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("You are now opted-out.")));
            }
            case "optin" -> {
                prefs.setOptOut(uuid, false).thenRun(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("You are now opted-in.")));
            }
            case "prefs" -> {
                UUID target = uuid;
                if (args.length >= 2 && sender.hasPermission("heneria.opt.admin")) {
                    OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
                    if (off.hasPlayedBefore() || off.isOnline()) {
                        target = off.getUniqueId();
                    }
                }
                prefs.getPrefs(target).thenAccept(p -> Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage("Prefs: opted_out=" + p.isOptedOut() + ", auto_apply_skin=" + p.isAutoApplySkin())));
            }
            default -> sender.sendMessage("Unknown subcommand");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
