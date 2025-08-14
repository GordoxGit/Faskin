package fr.heneriacore.claim;

import fr.heneriacore.HeneriaCore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimManager {
    private final HeneriaCore plugin;
    private final Map<UUID, ClaimSession> sessions = new ConcurrentHashMap<>();
    private final ImageGenerator generator = new ImageGenerator();
    private final long ttlSeconds;

    public ClaimManager(HeneriaCore plugin, long ttlSeconds) {
        this.plugin = plugin;
        this.ttlSeconds = ttlSeconds;
    }

    public CompletableFuture<ClaimSession> startClaim(UUID requesterUuid, String targetName) {
        return CompletableFuture.supplyAsync(() -> {
            UUID id = UUID.randomUUID();
            byte[] token = generator.randomToken(16);
            String hex = generator.toHex(token);
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(ttlSeconds);
            ClaimSession session = new ClaimSession(id, hex, now, exp, requesterUuid, targetName);
            File dir = new File(plugin.getDataFolder(), "claims");
            dir.mkdirs();
            File out = new File(dir, id + ".png");
            try {
                byte[] img = generator.generate(token);
                Files.write(out.toPath(), img);
            } catch (IOException e) {
                throw new RuntimeException("Cannot write claim image", e);
            }
            sessions.put(id, session);
            return session;
        });
    }

    public CompletableFuture<Boolean> checkClaim(UUID requesterUuid, String tokenId) {
        return CompletableFuture.supplyAsync(() -> {
            UUID id = UUID.fromString(tokenId);
            ClaimSession session = sessions.get(id);
            if (session == null) return false;
            if (!session.getRequesterUuid().equals(requesterUuid)) return false;
            if (Instant.now().isAfter(session.getExpiresAt())) {
                session.setState(ClaimSession.State.FAILED);
                return false;
            }
            session.setState(ClaimSession.State.VERIFIED);
            return true;
        });
    }

    public void cancelClaim(UUID requesterUuid) {
        sessions.values().removeIf(s -> s.getRequesterUuid().equals(requesterUuid));
    }

    public void cleanupExpired() {
        Instant now = Instant.now();
        sessions.values().removeIf(s -> now.isAfter(s.getExpiresAt()));
    }
}
