package com.faskin.auth.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryAccountRepository implements AccountRepository {
    private final Map<String, StoredAccount> store = new ConcurrentHashMap<>();
    private final com.faskin.auth.security.Pbkdf2Hasher hasher;

    public InMemoryAccountRepository(com.faskin.auth.security.Pbkdf2Hasher hasher) {
        this.hasher = hasher;
    }

    @Override public boolean exists(String usernameLower) {
        return store.containsKey(usernameLower);
    }

    @Override public void create(String usernameLower, byte[] salt, byte[] hash) {
        store.put(usernameLower, new StoredAccount(usernameLower, salt, hash));
    }

    @Override public Optional<StoredAccount> find(String usernameLower) {
        return Optional.ofNullable(store.get(usernameLower));
    }

    @Override public void updatePassword(String usernameLower, byte[] newSalt, byte[] newHash) {
        store.computeIfPresent(usernameLower, (k, v) -> new StoredAccount(k, newSalt, newHash));
    }

    // Utilitaire pour tests
    public boolean verify(String usernameLower, char[] raw) {
        var opt = find(usernameLower);
        if (opt.isEmpty()) return false;
        var acc = opt.get();
        return hasher.verify(raw, acc.salt, acc.hash);
    }
}
