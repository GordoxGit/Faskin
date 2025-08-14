package fr.heneriacore.premium;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PremiumAuthService {
    CompletableFuture<Boolean> autoLogin(UUID uuid, GameProfile profile);
}
