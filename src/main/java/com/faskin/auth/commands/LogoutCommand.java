package com.faskin.auth.commands;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.command.*;

public final class LogoutCommand implements CommandExecutor {
    private final FaskinPlugin plugin;
    public LogoutCommand(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player p)) { sender.sendMessage("[Faskin] In-game uniquement."); return true; }
        plugin.services().setState(p.getUniqueId(), PlayerAuthState.REGISTERED_UNAUTH);
        p.sendMessage(plugin.messages().prefixed("logout_ok"));
        return true;
    }
}
