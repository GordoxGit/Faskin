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

    public InMemoryAccountRepository(com.faskin.auth.security.Pbkdf2Hasher hasher) { this.hasher = hasher; }

    @Override public boolean exists(String usernameLower) { return store.containsKey(usernameLower); }

    @Override public void create(String usernameLower, byte[] salt, byte[] hash) {
        store.put(usernameLower, new StoredAccount(usernameLower, salt, hash));
        fails.remove(usernameLower); locked.remove(usernameLower);
    }

    @Override public Optional<StoredAccount> find(String usernameLower) { return Optional.ofNullable(store.get(usernameLower)); }

    @Override public void updatePassword(String usernameLower, byte[] newSalt, byte[] newHash) {
        store.computeIfPresent(usernameLower, (k, v) -> new StoredAccount(k, newSalt, newHash));
    }

    @Override public void updateLastLoginAndIp(String usernameLower, String ip, long epochSeconds) {
        meta.put(usernameLower, new SessionMeta(ip, epochSeconds));
        fails.remove(usernameLower); locked.remove(usernameLower);
    }

    @Override public Optional<SessionMeta> getSessionMeta(String usernameLower) { return Optional.ofNullable(meta.get(usernameLower)); }

    @Override public boolean isLocked(String usernameLower) {
        long until = locked.getOrDefault(usernameLower, 0L);
        return until > (System.currentTimeMillis() / 1000L);
    }

    @Override public void registerFailedAttempt(String usernameLower, int max, long lockSeconds) {
        int f = fails.getOrDefault(usernameLower, 0) + 1;
        if (f >= max) {
            long until = (System.currentTimeMillis() / 1000L) + lockSeconds;
            locked.put(usernameLower, until);
            fails.put(usernameLower, 0);
        } else {
            fails.put(usernameLower, f);
        }
    }

    @Override public void resetFailures(String usernameLower) { fails.remove(usernameLower); locked.remove(usernameLower); }

    @Override public long lockRemainingSeconds(String usernameLower) {
        long until = locked.getOrDefault(usernameLower, 0L);
        long now = System.currentTimeMillis() / 1000L;
        return Math.max(0L, until - now);
    }

    @Override public int countAccounts() { return store.size(); }

    @Override public int countLockedActive(long nowEpochSeconds) {
        int c = 0;
        for (long until : locked.values()) if (until > nowEpochSeconds) c++;
        return c;
    }

    @Override public Optional<AdminInfo> adminInfo(String usernameLower) {
        boolean ex = store.containsKey(usernameLower);
        var m = meta.get(usernameLower);
        int f = fails.getOrDefault(usernameLower, 0);
        long lock = locked.getOrDefault(usernameLower, 0L);
        String ip = m != null ? m.lastIp : null;
        long last = m != null ? m.lastLoginEpoch : 0L;
        return Optional.of(new AdminInfo(ex, ip, last, f, lock));
    }

    // Utilitaire tests
    public boolean verify(String usernameLower, char[] raw) {
        var opt = find(usernameLower);
        if (opt.isEmpty()) return false;
        var acc = opt.get();
        return hasher.verify(raw, acc.salt, acc.hash);
    }
}
