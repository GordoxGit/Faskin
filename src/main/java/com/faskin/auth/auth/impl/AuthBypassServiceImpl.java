package com.faskin.auth.auth.impl;

import com.faskin.auth.FaskinPlugin;
import com.faskin.auth.auth.AuthBypassService;
import com.faskin.auth.core.AccountRepository;
import com.faskin.auth.core.PlayerAuthState;
import com.faskin.auth.premium.PremiumMode;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public final class AuthBypassServiceImpl implements AuthBypassService {
    private final FaskinPlugin plugin;
    private final AccountRepository repo;

    public AuthBypassServiceImpl(FaskinPlugin plugin, AccountRepository repo) {
        this.plugin = plugin;
        this.repo = repo;
    }

    @Override
    public void markAuthenticated(UUID playerId, String name, @Nullable String uuidOnline, PremiumMode mode) {
        var player = Bukkit.getPlayer(playerId);
        String ip = null;
        if (player != null && player.getAddress() != null && player.getAddress().getAddress() != null) {
            ip = player.getAddress().getAddress().getHostAddress();
        }

        plugin.getTimeouts().cancel(playerId);
        plugin.services().setState(playerId, PlayerAuthState.AUTHENTICATED);

        String finalIp = ip;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String key = name.toLowerCase();
            long now = Instant.now().getEpochSecond();
            if (!repo.exists(key) && plugin.configs().premiumAutoRegister()) {
                repo.create(key, new byte[0], new byte[0]);
            }
            repo.updatePremiumInfo(key, true, uuidOnline, mode.name(), now);
            if (plugin.configs().allowIpSession() && plugin.configs().sessionMinutes() > 0 && finalIp != null) {
                repo.updateLastLoginAndIp(key, finalIp, now);
            }
        });

        String shortUuid = uuidOnline != null && uuidOnline.length() >= 8 ? uuidOnline.substring(0, 8) : "unknown";
        plugin.getLogger().info("Premium bypass OK: " + name + " " + shortUuid + " (mode=" + mode + ")");
    }
}
