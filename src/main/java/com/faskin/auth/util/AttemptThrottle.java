package com.faskin.auth.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AttemptThrottle {
    private final Map<UUID, Long> last = new ConcurrentHashMap<>();

    public boolean canAttempt(UUID uuid, long minIntervalSeconds) {
        long now = System.currentTimeMillis();
        long minMs = Math.max(0L, minIntervalSeconds) * 1000L;
        Long prev = last.get(uuid);
        if (prev == null || now - prev >= minMs) {
            last.put(uuid, now);
            return true;
        }
        return false;
    }

    public long secondsLeft(UUID uuid, long minIntervalSeconds) {
        long now = System.currentTimeMillis();
        long minMs = Math.max(0L, minIntervalSeconds) * 1000L;
        Long prev = last.get(uuid);
        if (prev == null) return 0L;
        long rem = (prev + minMs - now + 999) / 1000; // ceil
        return Math.max(0L, rem);
    }

    public void clear(UUID uuid) { last.remove(uuid); }
}
