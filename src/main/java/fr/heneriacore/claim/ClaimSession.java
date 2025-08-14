package fr.heneriacore.claim;

import java.time.Instant;
import java.util.UUID;

public class ClaimSession {
    public enum State {PENDING, VERIFIED, CANCELLED, FAILED}

    private final UUID id;
    private final String tokenHex;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final UUID requesterUuid;
    private final String targetName;
    private State state;
    private int attempts;

    public ClaimSession(UUID id, String tokenHex, Instant createdAt, Instant expiresAt,
                        UUID requesterUuid, String targetName) {
        this.id = id;
        this.tokenHex = tokenHex;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.requesterUuid = requesterUuid;
        this.targetName = targetName;
        this.state = State.PENDING;
        this.attempts = 0;
    }

    public UUID getId() {return id;}
    public String getTokenHex() {return tokenHex;}
    public Instant getCreatedAt() {return createdAt;}
    public Instant getExpiresAt() {return expiresAt;}
    public UUID getRequesterUuid() {return requesterUuid;}
    public String getTargetName() {return targetName;}
    public State getState() {return state;}
    public void setState(State state) {this.state = state;}
    public int getAttempts() {return attempts;}
    public void incrementAttempts() {this.attempts++;}
}
