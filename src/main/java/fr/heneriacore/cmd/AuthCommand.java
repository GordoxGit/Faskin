package fr.heneriacore.cmd;

import fr.heneriacore.HeneriaCore;
import fr.heneriacore.auth.AuthManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuthCommand implements CommandExecutor, TabCompleter {
    private final HeneriaCore plugin;
    private final AuthManager auth;
    private final ClaimCommand claimCommand;

    public AuthCommand(HeneriaCore plugin, ClaimCommand claimCommand) {
        this.plugin = plugin;
        this.auth = plugin.getAuthManager();
        this.claimCommand = claimCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("Usage: /" + label + " register|login|logout");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("claim")) {
            return claimCommand.onCommand(sender, command, label, args);
        }
        switch (sub) {
            case "register" -> {
                if (args.length < 2) {
                    player.sendMessage("Usage: /" + label + " register <password>");
                    return true;
                }
                char[] pw = args[1].toCharArray();
                auth.register(player.getUniqueId(), player.getName(), pw).thenAccept(success ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (success) {
                                player.sendMessage("Registered.");
                                if (plugin.getConfig().getBoolean("auth.autoLoginAfterRegister", true)) {
                                    auth.login(player.getUniqueId(), player.getName(), args[1].toCharArray());
                                }
                            } else {
                                player.sendMessage("Already registered.");
                            }
                        }));
            }
            case "login" -> {
                if (args.length < 2) {
                    player.sendMessage("Usage: /" + label + " login <password>");
                    return true;
                }
                auth.login(player.getUniqueId(), player.getName(), args[1].toCharArray()).thenAccept(opt ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (opt.isPresent()) {
                                player.sendMessage("Logged in.");
                            } else {
                                player.sendMessage("Invalid credentials.");
                            }
                        }));
            }
            case "logout" -> {
                Optional<String> token = auth.getToken(player.getUniqueId());
                if (token.isEmpty()) {
                    player.sendMessage("Not logged in.");
                    return true;
                }
                auth.logout(token.get()).thenAccept(res ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (res) {
                                player.sendMessage("Logged out.");
                            } else {
                                player.sendMessage("Logout failed.");
                            }
                        }));
            }
            default -> player.sendMessage("Usage: /" + label + " register|login|logout|claim");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("register", "login", "logout", "claim");
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("claim")) {
            return claimCommand.onTabComplete(sender, command, alias, args);
        }
        return Collections.emptyList();
    }
}
