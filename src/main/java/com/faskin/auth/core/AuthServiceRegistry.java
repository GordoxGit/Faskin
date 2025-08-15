package com.faskin.auth.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthServiceRegistry {
    private final AccountRepository accounts;
    private final Map<UUID, PlayerAuthState> states = new ConcurrentHashMap<>();

    public AuthServiceRegistry(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public AccountRepository accounts() { return accounts; }

    public PlayerAuthState getState(UUID uuid) {
        return states.getOrDefault(uuid, PlayerAuthState.UNREGISTERED);
    }

    public void setState(UUID uuid, PlayerAuthState state) {
        states.put(uuid, state);
    }

    public void clearState(UUID uuid) { states.remove(uuid); }

    public Map<PlayerAuthState,Integer> countStates() {
        Map<PlayerAuthState,Integer> m = new EnumMap<>(PlayerAuthState.class);
        for (var st : states.values()) m.put(st, m.getOrDefault(st, 0) + 1);
        return m;
    }
}
