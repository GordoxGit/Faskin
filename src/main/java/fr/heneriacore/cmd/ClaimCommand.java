package fr.heneriacore.cmd;

import fr.heneriacore.HeneriaCore;
import fr.heneriacore.claim.ClaimManager;
import fr.heneriacore.claim.ClaimSession;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClaimCommand implements TabCompleter {
    private final HeneriaCore plugin;
    private final ClaimManager claimManager;

    public ClaimCommand(HeneriaCore plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("Usage: /" + label + " claim <start|check|cancel|status>" );
            return true;
        }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "start" -> {
                if (args.length < 3) {
                    player.sendMessage("Usage: /" + label + " claim start <targetName>");
                    return true;
                }
                String target = args[2];
                CompletableFuture<ClaimSession> fut = claimManager.startClaim(player.getUniqueId(), target);
                fut.thenAccept(session -> Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage("Claim started. Token: " + session.getId())));
            }
            case "check" -> {
                if (args.length < 3) {
                    player.sendMessage("Usage: /" + label + " claim check <tokenId>");
                    return true;
                }
                String token = args[2];
                claimManager.checkClaim(player.getUniqueId(), token).thenAccept(ok ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (ok) player.sendMessage("Claim verified");
                            else player.sendMessage("Claim failed");
                        }));
            }
            case "cancel" -> claimManager.cancelClaim(player.getUniqueId());
            case "status" -> player.sendMessage("No status tracking implemented");
            default -> player.sendMessage("Unknown subcommand");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("start", "check", "cancel", "status");
        }
        return Collections.emptyList();
    }
}
