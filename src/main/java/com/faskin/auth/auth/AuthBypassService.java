package com.faskin.auth.auth;

import com.faskin.auth.premium.PremiumMode;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface AuthBypassService {
    void markAuthenticated(UUID playerId, String name, @Nullable String uuidOnline, PremiumMode mode);
}
