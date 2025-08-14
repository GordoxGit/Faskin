package com.heneria.skinview.service.impl;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.service.SkinApplier;
import com.heneria.skinview.service.SkinDescriptor;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinApplierProtocolLib implements SkinApplier, ListenerPriorityAccessor {
    private final SkinviewPlugin plugin;
    private final ProtocolManager manager;
    private final Map<UUID, SignedTex> pending = new ConcurrentHashMap<>();
    private PacketAdapter adapter;

    public SkinApplierProtocolLib(SkinviewPlugin plugin) {
        this.plugin = plugin;
        this.manager = ProtocolLibrary.getProtocolManager();
        register();
        plugin.getLogger().info("[skinview] ProtocolLib applier actif.");
    }

    private void register() {
        adapter = new PacketAdapter(plugin, getPriority(),
                PacketType.Play.Server.PLAYER_INFO,
                PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            @Override public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // Actions (ADD_PLAYER requis)
                Set<EnumWrappers.PlayerInfoAction> actions = EnumSet.noneOf(EnumWrappers.PlayerInfoAction.class);
                try {
                    actions = packet.getPlayerInfoActions().read(0);
                } catch (Exception ignored) { /* older variants may not expose actions here */ }

                boolean addPlayer = actions.isEmpty() || actions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER);

                List<PlayerInfoData> list = packet.getPlayerInfoDataLists().read(0);
                boolean modified = false;
                List<PlayerInfoData> out = new ArrayList<>(list.size());

                for (PlayerInfoData pid : list) {
                    WrappedGameProfile old = pid.getProfile();
                    UUID uuid = old.getUUID();
                    SignedTex st = pending.get(uuid);

                    if (st != null && addPlayer) {
                        WrappedGameProfile gp = new WrappedGameProfile(uuid, old.getName());
                        gp.getProperties().clear();
                        gp.getProperties().put("textures", new WrappedSignedProperty("textures", st.value(), st.signature()));

                        PlayerInfoData npid = new PlayerInfoData(
                                gp,
                                pid.getLatency(),
                                pid.getGameMode(),
                                pid.getDisplayName()
                        );
                        out.add(npid);
                        modified = true;
                        // retire la demande après premier envoi
                        pending.remove(uuid);
                    } else {
                        out.add(pid);
                    }
                }

                if (modified) {
                    packet.getPlayerInfoDataLists().write(0, out);
                }
            }
        };
        manager.addPacketListener(adapter);
    }

    @Override public void apply(Player player, SkinDescriptor descriptor) throws Exception {
        if (!descriptor.hasSignedTextures())
            throw new IllegalArgumentException("Signed textures required (value+signature)");
        pending.put(player.getUniqueId(), new SignedTex(descriptor.texturesValueB64(), descriptor.texturesSignature()));

        // Déclenchement des paquets ADD_PLAYER pour les autres
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!viewer.equals(player)) viewer.hidePlayer(plugin, player);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewer.equals(player)) viewer.showPlayer(plugin, player);
                }
            }, 2L);
        });
    }

    @Override public void clear(Player player) {
        pending.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers())
                if (!viewer.equals(player)) viewer.hidePlayer(plugin, player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player viewer : Bukkit.getOnlinePlayers())
                    if (!viewer.equals(player)) viewer.showPlayer(plugin, player);
            }, 2L);
        });
    }

    public void shutdown() {
        if (adapter != null) {
            manager.removePacketListener(adapter);
            adapter = null;
        }
        pending.clear();
    }

    // Priorité haute mais non bloquante
    @Override public ListenerPriority getPriority() {
        return ListenerPriority.HIGH;
    }

    private record SignedTex(String value, String signature) {}
    private interface ListenerPriorityAccessor { ListenerPriority getPriority(); }
}
