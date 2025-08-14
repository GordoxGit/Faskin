package fr.heneriacore.skin;

import org.bukkit.entity.Player;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public interface SkinService {
    CompletableFuture<Void> applySigned(Player target, SignedTexture texture);
    CompletableFuture<Void> applyUnsigned(Player target, URI textureUrl);
    void shutdown();
}
