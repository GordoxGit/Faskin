package com.heneria.skinview.service.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinApplier;
import com.heneria.skinview.service.SkinDescriptor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Applier via ProtocolLib: réécriture PLAYER_INFO_UPDATE. */
public final class SkinApplierProtocolLib implements SkinApplier {

    private final SkinviewPlugin plugin;
    private final ProtocolManager pm;
    private final ConcurrentHashMap<UUID, SkinProperty> pending = new ConcurrentHashMap<>();
    private PacketAdapter listener;

    public SkinApplierProtocolLib(SkinviewPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.pm = ProtocolLibrary.getProtocolManager();
        this.listener = new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Set<EnumWrappers.PlayerInfoAction> actions = event.getPacket().getPlayerInfoActions().read(0);
                if (!actions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) return;

                List<PlayerInfoData> list = event.getPacket().getPlayerInfoDataLists().read(0);
                List<PlayerInfoData> out = new ArrayList<>(list.size());
                boolean modified = false;

                for (PlayerInfoData pid : list) {
                    UUID id = pid.getProfile().getUUID();
                    SkinProperty prop = pending.remove(id);
                    if (prop != null) {
                        WrappedGameProfile gp = new WrappedGameProfile(id, pid.getProfile().getName());
                        gp.getProperties().put("textures", new WrappedSignedProperty("textures", prop.value(), prop.signature()));
                        PlayerInfoData npid = new PlayerInfoData(gp, pid.getLatency(), pid.getGameMode(), pid.getDisplayName(), pid.getListed());
                        out.add(npid);
                        modified = true;
                    } else {
                        out.add(pid);
                    }
                }

                if (modified) {
                    event.getPacket().getPlayerInfoDataLists().write(0, out);
                }
            }
        };
        pm.addPacketListener(listener);
        plugin.getLogger().info("Using ProtocolLib applier");
    }

    @Override
    public void apply(Player player, SkinDescriptor sd) {
        String value = sd.texturesValueBase64();
        String sig = sd.texturesSignature();
        if (value == null || sig == null) {
            // Tentative de re-résolution nominative
            plugin.resolver().resolveByPremiumName(player.getName()).whenComplete((nsd, ex) -> {
                if (nsd != null && ex == null) {
                    Bukkit.getScheduler().runTask(plugin, () -> apply(player, nsd));
                }
            });
            return;
        }

        pending.put(player.getUniqueId(), new SkinProperty(value, sig));

        for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.hidePlayer(plugin, player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.showPlayer(plugin, player);
        }, 2L);

        plugin.getLogger().fine("[skinview] Applied premium skin (ProtocolLib path)");
    }

    @Override
    public void clear(Player player) {
        pending.remove(player.getUniqueId());
        for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.hidePlayer(plugin, player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) if (!viewer.equals(player)) viewer.showPlayer(plugin, player);
        }, 2L);
    }

    @Override
    public void shutdown() {
        if (listener != null) {
            pm.removePacketListener(listener);
            listener = null;
        }
        pending.clear();
    }

    private record SkinProperty(String value, String signature) {}
}

