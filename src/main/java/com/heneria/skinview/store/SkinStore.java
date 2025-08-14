package com.heneria.skinview.store;

import com.heneria.skinview.service.SkinDescriptor;

import java.util.Optional;
import java.util.UUID;

public interface SkinStore {
    Optional<SkinRecord> get(UUID playerUuid);
    void put(UUID playerUuid, SkinDescriptor descriptor);
    boolean clear(UUID playerUuid);
    long ttlSeconds();
}

