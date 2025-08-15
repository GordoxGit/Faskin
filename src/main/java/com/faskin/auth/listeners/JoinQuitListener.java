package com.faskin.auth.listeners;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.core.PlayerAuthState;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Objects;

public final class JoinQuitListener implements Listener {
    private final FaskinPlugin plugin;

    public JoinQuitListener(FaskinPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        final String user = p.getName().toLowerCase();
        final InetSocketAddress sock = p.getAddress();
        final String ip = (sock != null && sock.getAddress() != null) ? sock.getAddress().getHostAddress() : "unknown";

        plugin.services().setState(p.getUniqueId(), PlayerAuthState.UNREGISTERED);

        final boolean allow = plugin.configs().allowIpSession();
        final int ttlMin = plugin.configs().sessionMinutes();
        final boolean chatOnJoin = plugin.getConfig().getBoolean("reminder.chat_on_join", false);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            var repo = plugin.services().accounts();
            boolean exists = repo.exists(user);

            if (!exists) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (chatOnJoin) p.sendMessage(plugin.messages().prefixed("reminder_chat"));
                    plugin.getTimeouts().schedule(p);
                    p.sendMessage(plugin.messages().prefixed("must_register"));
                });
                return;
            }

            if (allow && ttlMin > 0) {
                var metaOpt = repo.getSessionMeta(user);
                if (metaOpt.isPresent()) {
                    var meta = metaOpt.get();
                    long now = Instant.now().getEpochSecond();
                    boolean ipMatch = Objects.equals(meta.lastIp, ip) && !"unknown".equals(ip);
                    boolean within = meta.lastLoginEpoch > 0 && (now - meta.lastLoginEpoch) <= (ttlMin * 60L);

                    if (ipMatch && within) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.services().setState(p.getUniqueId(), PlayerAuthState.AUTHENTICATED);
                            p.sendMessage(plugin.messages().prefixed("login_ok"));
                        });
                        return;
                    }
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (chatOnJoin) p.sendMessage(plugin.messages().prefixed("reminder_chat"));
                plugin.services().setState(p.getUniqueId(), PlayerAuthState.REGISTERED_UNAUTH);
                plugin.getTimeouts().schedule(p);
                p.sendMessage(plugin.messages().prefixed("must_login"));
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.services().clearState(e.getPlayer().getUniqueId());
        plugin.getTimeouts().cancel(e.getPlayer().getUniqueId());
    }
}
