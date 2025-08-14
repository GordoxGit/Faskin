package com.heneria.skinview.net;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple token-bucket rate limiter. Thread-safe.
 */
public final class RateLimiter {
    private final double ratePerMillis;
    private final int burst;
    private double tokens;
    private long lastRefillNanos;
    private final ReentrantLock lock = new ReentrantLock();

    public RateLimiter(int requestsPerMinute, int burst) {
        if (requestsPerMinute <= 0) throw new IllegalArgumentException("rpm <= 0");
        if (burst <= 0) throw new IllegalArgumentException("burst <= 0");
        this.ratePerMillis = requestsPerMinute / 60000.0;
        this.burst = burst;
        this.tokens = burst;
        this.lastRefillNanos = System.nanoTime();
    }

    /** Attempt to consume one token. */
    public boolean tryConsume() {
        lock.lock();
        try {
            refillLocked();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void refillLocked() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) return;
        double newTokens = elapsedNanos / 1_000_000.0 * ratePerMillis;
        if (newTokens > 0) {
            tokens = Math.min(burst, tokens + newTokens);
            lastRefillNanos = now;
        }
    }
}
