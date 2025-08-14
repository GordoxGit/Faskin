package com.heneria.skinview.service;

import com.heneria.skinview.SkinviewPlugin;
import com.heneria.skinview.store.SkinRecord;
import com.heneria.skinview.store.SkinStore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SkinService {

    private final SkinviewPlugin plugin;
    private final SkinResolver resolver;
    private final SkinApplier applier;
    private final SkinStore store;

    public SkinService(SkinviewPlugin plugin, SkinResolver resolver, SkinApplier applier, SkinStore store) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.applier = Objects.requireNonNull(applier, "applier");
        this.store = Objects.requireNonNull(store, "store");
    }

    public CompletableFuture<Void> applyByPremiumName(CommandSender actor, Player target, String name) {
        final String targetName = target.getName();
        info(actor, "apply-start", targetName);
        return resolver.resolveByPremiumName(name)
                .thenAccept(sd -> Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        applier.apply(target, sd);
                        store.put(target.getUniqueId(), sd);
                        ok(actor, "apply-ok", targetName, sd.model().name());
                    } catch (Exception e) {
                        fail(actor, "apply-fail", e.getMessage());
                    }
                }))
                .exceptionally(ex -> { fail(actor, "resolve-fail", ex.getMessage()); return null; });
    }

    public CompletableFuture<Void> applyByTexturesUrl(CommandSender actor, Player target, String url) {
        final String targetName = target.getName();
        info(actor, "apply-start", targetName);
        return resolver.resolveByTexturesUrl(url)
                .thenAccept(sd -> Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        applier.apply(target, sd);
                        store.put(target.getUniqueId(), sd);
                        ok(actor, "apply-ok", targetName, sd.model().name());
                        if (sd.hasSignedTextures()) info(actor, "signature-found", null);
                        else info(actor, "signature-missing", null);
                    } catch (Exception e) {
                        fail(actor, "apply-fail", e.getMessage());
                    }
                }))
                .exceptionally(ex -> { fail(actor, "resolve-fail", ex.getMessage()); return null; });
    }

    /** Applique immédiatement à partir du store (si présent & TTL OK). */
    public boolean applyFromStore(CommandSender actor, Player target) {
        UUID id = target.getUniqueId();
        var opt = store.get(id);
        if (opt.isEmpty()) return false;
        SkinRecord rec = opt.get();
        SkinDescriptor sd = new SkinDescriptor(rec.skinUrl(), rec.model(), rec.texturesValueB64(), rec.texturesSignature());
        try {
            applier.apply(target, sd);
            ok(actor, "apply-ok", target.getName(), sd.model().name());
            return true;
        } catch (Exception e) {
            fail(actor, "apply-fail", e.getMessage());
            return false;
        }
    }

    public boolean clearCache(CommandSender actor, Player target) {
        boolean ok = store.clear(target.getUniqueId());
        if (ok) ok(actor, "clear-ok", target.getName(), null);
        else fail(actor, "clear-fail", "not-found");
        return ok;
    }

    /* helpers messages */
    private void info(CommandSender s, String path, String player) {
        send(s, path, player, null);
    }
    private void ok(CommandSender s, String path, String player, String model) {
        send(s, path, player, model);
    }
    private void fail(CommandSender s, String path, String error) {
        String msg = plugin.messages().getString(path, "&cErreur.");
        msg = msg.replace("%error%", error == null ? "unknown" : error);
        s.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                plugin.messages().getString("prefix", "") + msg));
    }
    private void send(CommandSender s, String path, String player, String model) {
        String msg = plugin.messages().getString(path, "&7.");
        if (player != null) msg = msg.replace("%player%", player);
        if (model != null) msg = msg.replace("%model%", model);
        s.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                plugin.messages().getString("prefix", "") + msg));
    }
}

