package com.faskin.auth.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthServiceRegistry {
    private final AccountRepository accounts;
    private final Map<UUID, PlayerAuthState> states = new ConcurrentHashMap<>();
    private final com.faskin.auth.premium.PremiumDetector premiumDetector;
    private final com.faskin.auth.auth.AuthBypassService authBypass;

    public AuthServiceRegistry(AccountRepository accounts, com.faskin.auth.premium.PremiumDetector detector, com.faskin.auth.auth.AuthBypassService bypass) {
        this.accounts = accounts;
        this.premiumDetector = detector;
        this.authBypass = bypass;
    }

    public AccountRepository accounts() { return accounts; }
    public com.faskin.auth.premium.PremiumDetector premiumDetector() { return premiumDetector; }
    public com.faskin.auth.auth.AuthBypassService authBypass() { return authBypass; }

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
