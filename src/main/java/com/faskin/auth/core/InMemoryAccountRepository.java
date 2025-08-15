package com.faskin.auth.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryAccountRepository implements AccountRepository {
    private final Map<String, StoredAccount> store = new ConcurrentHashMap<>();
    private final Map<String, SessionMeta> meta = new ConcurrentHashMap<>();
    private final Map<String, Integer> fails = new ConcurrentHashMap<>();
    private final Map<String, Long> locked = new ConcurrentHashMap<>();

    private final com.faskin.auth.security.Pbkdf2Hasher hasher;

    public InMemoryAccountRepository(com.faskin.auth.security.Pbkdf2Hasher hasher) {
        this.hasher = hasher;
    }

    @Override public boolean exists(String usernameLower) {
        return store.containsKey(usernameLower);
    }

    @Override public void create(String usernameLower, byte[] salt, byte[] hash) {
        store.put(usernameLower, new StoredAccount(usernameLower, salt, hash));
        fails.remove(usernameLower); locked.remove(usernameLower);
    }

    @Override public Optional<StoredAccount> find(String usernameLower) {
        return Optional.ofNullable(store.get(usernameLower));
    }

    @Override public void updatePassword(String usernameLower, byte[] newSalt, byte[] newHash) {
        store.computeIfPresent(usernameLower, (k, v) -> new StoredAccount(k, newSalt, newHash));
    }

    @Override public void updateLastLoginAndIp(String usernameLower, String ip, long epochSeconds) {
        meta.put(usernameLower, new SessionMeta(ip, epochSeconds));
    }

    @Override public Optional<SessionMeta> getSessionMeta(String usernameLower) {
        return Optional.ofNullable(meta.get(usernameLower));
    }

    @Override public boolean isLocked(String usernameLower) {
        long until = locked.getOrDefault(usernameLower, 0L);
        return until > (System.currentTimeMillis() / 1000L);
    }

    @Override public void registerFailedAttempt(String usernameLower, int max, long lockSeconds) {
        int f = fails.getOrDefault(usernameLower, 0) + 1;
        fails.put(usernameLower, f);
        if (f >= max) {
            long until = (System.currentTimeMillis() / 1000L) + lockSeconds;
            locked.put(usernameLower, until);
            fails.put(usernameLower, 0);
        }
    }

    @Override public void resetFailures(String usernameLower) {
        fails.remove(usernameLower); locked.remove(usernameLower);
    }

    @Override public long lockRemainingSeconds(String usernameLower) {
        long until = locked.getOrDefault(usernameLower, 0L);
        long now = System.currentTimeMillis() / 1000L;
        return Math.max(0L, until - now);
    }

    // Utilitaire tests
    public boolean verify(String usernameLower, char[] raw) {
        var opt = find(usernameLower);
        if (opt.isEmpty()) return false;
        var acc = opt.get();
        return hasher.verify(raw, acc.salt, acc.hash);
    }
}
