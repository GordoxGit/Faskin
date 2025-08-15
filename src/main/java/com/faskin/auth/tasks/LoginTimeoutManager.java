package com.faskin.auth.tasks;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginTimeoutManager {
    private final FaskinPlugin plugin;
    private final Map<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();

    public LoginTimeoutManager(FaskinPlugin plugin) { this.plugin = plugin; }

    public void schedule(Player p) {
        cancel(p.getUniqueId());
        int seconds = Math.max(5, plugin.configs().loginTimeoutSeconds());
        boolean kick = plugin.configs().kickOnTimeout();

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            var state = plugin.services().getState(p.getUniqueId());
            if (state != PlayerAuthState.AUTHENTICATED) {
                if (kick) {
                    p.kickPlayer(plugin.messages().prefixed("timeout_kick"));
                } else {
                    p.sendMessage(plugin.messages().prefixed("timeout_chat"));
                }
            }
            tasks.remove(p.getUniqueId());
        }, seconds * 20L);
        tasks.put(p.getUniqueId(), task);
    }

    public void cancel(UUID uuid) {
        BukkitTask t = tasks.remove(uuid);
        if (t != null) t.cancel();
    }

    public void cancelAll() {
        for (var t : tasks.values()) t.cancel();
        tasks.clear();
    }
}
