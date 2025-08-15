package com.faskin.auth.tasks;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.PlayerAuthState;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class AuthReminderTask extends BukkitRunnable {
    private final FaskinPlugin plugin;

    public AuthReminderTask(FaskinPlugin plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("reminder.enabled", true)) return;
        boolean useActionBar = plugin.getConfig().getBoolean("reminder.actionbar", true);
        String msg = plugin.messages().prefixed("reminder_actionbar");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.services().getState(p.getUniqueId()) != PlayerAuthState.AUTHENTICATED) {
                if (useActionBar) {
                    // Spigot API: action bar via Player.Spigot#sendMessage(ChatMessageType.ACTION_BAR,...)
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                } else {
                    p.sendMessage(plugin.messages().prefixed("reminder_chat"));
                }
            }
        }
    }
}
