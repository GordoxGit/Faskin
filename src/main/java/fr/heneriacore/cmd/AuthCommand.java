package fr.heneriacore.cmd;

import fr.heneriacore.auth.AuthManager;
import fr.heneriacore.event.AuthPreLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class AuthCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final AuthManager authManager;

    public AuthCommand(Plugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("/heneria <register|login|logout>");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "register" -> {
                if (args.length < 2) {
                    player.sendMessage("Usage: /heneria register <password>");
                    return true;
                }
                char[] pass = args[1].toCharArray();
                authManager.register(player.getUniqueId(), player.getName(), pass)
                        .thenAccept(res -> Bukkit.getScheduler().runTask(plugin, () -> {
                            if (res) {
                                player.sendMessage("Registered.");
                                if (plugin.getConfig().getBoolean("auth.autoLoginAfterRegister", true)) {
                                    login(player, pass);
                                }
                            } else {
                                player.sendMessage("Already registered.");
                            }
                        }));
                return true;
            }
            case "login" -> {
                if (args.length < 2) {
                    player.sendMessage("Usage: /heneria login <password>");
                    return true;
                }
                char[] pass = args[1].toCharArray();
                AuthPreLoginEvent pre = new AuthPreLoginEvent(player);
                Bukkit.getPluginManager().callEvent(pre);
                if (pre.isCancelled()) {
                    player.sendMessage("Login cancelled.");
                    return true;
                }
                login(player, pass);
                return true;
            }
            case "logout" -> {
                authManager.getToken(player.getUniqueId()).ifPresentOrElse(token -> {
                    authManager.logout(token).thenAccept(res -> Bukkit.getScheduler().runTask(plugin, () -> {
                        if (res) player.sendMessage("Logged out.");
                        else player.sendMessage("Not logged in.");
                    }));
                }, () -> player.sendMessage("Not logged in."));
                return true;
            }
            default -> {
                player.sendMessage("/heneria <register|login|logout>");
                return true;
            }
        }
    }

    private void login(Player player, char[] pass) {
        authManager.login(player.getUniqueId(), player.getName(), pass)
                .thenAccept(opt -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (opt.isPresent()) {
                        player.sendMessage("Login success.");
                    } else {
                        player.sendMessage("Login failed.");
                    }
                }));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("register", "login", "logout");
        }
        return List.of();
    }
}
