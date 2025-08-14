package fr.heneriacore.skin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinServiceImpl implements SkinService {
    private final Plugin plugin;
    private final TextureCache cache;
    private final SkinApplier applier;
    private final boolean refreshTablist;
    private final fr.heneriacore.prefs.PreferencesManager prefs;

    public SkinServiceImpl(Plugin plugin, TextureCache cache, boolean enableProtocolLib, boolean refreshTablist, fr.heneriacore.prefs.PreferencesManager prefs) {
        this.plugin = plugin;
        this.cache = cache;
        this.refreshTablist = refreshTablist;
        this.prefs = prefs;
        this.applier = detectApplier(enableProtocolLib);
    }

    private SkinApplier detectApplier(boolean enableProtocolLib) {
        if (enableProtocolLib && Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                return new ProtocolLibApplier();
            } catch (Throwable ignored) {
            }
        }
        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            return new PaperApplier();
        } catch (ClassNotFoundException ignored) {
        }
        plugin.getLogger().warning("No skin applier available; textures will only be cached");
        return null;
    }

    @Override
    public CompletableFuture<Void> applySigned(Player target, SignedTexture texture) {
        UUID uuid = target.getUniqueId();
        return prefs.isOptedOut(uuid).thenCompose(out -> {
            if (out) {
                return CompletableFuture.completedFuture(null);
            }
            Optional<SignedTexture> current = cache.get(uuid);
            if (current.isPresent() && SkinUtils.same(current.get(), texture)) {
                return CompletableFuture.completedFuture(null);
            }
            cache.put(uuid, texture);
            if (applier == null) {
                return CompletableFuture.completedFuture(null);
            }
            CompletableFuture<Void> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    applier.apply(plugin, target, texture, refreshTablist);
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            return future;
        });
    }

    @Override
    public CompletableFuture<Void> applyUnsigned(Player target, URI textureUrl) {
        cache.put(target.getUniqueId(), new SignedTexture(textureUrl.toString(), "", System.currentTimeMillis()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void shutdown() {
        cache.flush();
        if (applier != null) {
            applier.shutdown();
        }
    }

    public String getApplierName() {
        return applier == null ? "none" : applier.getClass().getSimpleName();
    }
}
