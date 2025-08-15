package com.faskin.auth.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiter {
    private final Map<UUID, Map<String, Long>> last = new ConcurrentHashMap<>();

    public boolean shouldNotify(UUID uuid, String key, long minIntervalMs) {
        long now = System.currentTimeMillis();
        Map<String, Long> m = last.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>());
        Long prev = m.get(key);
        if (prev == null || (now - prev) >= minIntervalMs) {
            m.put(key, now);
            return true;
        }
        return false;
    }

    public void clear(UUID uuid) { last.remove(uuid); }
}
