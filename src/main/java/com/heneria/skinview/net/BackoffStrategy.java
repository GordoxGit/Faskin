package com.heneria.skinview.net;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Exponential backoff with optional jitter.
 */
public final class BackoffStrategy {
    private final long baseDelayMs;
    private final long maxDelayMs;
    private final double jitter;

    public BackoffStrategy(long baseDelayMs, long maxDelayMs, double jitter) {
        this.baseDelayMs = Math.max(1, baseDelayMs);
        this.maxDelayMs = Math.max(this.baseDelayMs, maxDelayMs);
        this.jitter = Math.max(0.0, jitter);
    }

    /**
     * Compute delay for given retry attempt (0-based).
     */
    public long computeDelay(int attempt) {
        long delay = baseDelayMs << attempt; // base * 2^attempt
        if (delay < 0 || delay > maxDelayMs) delay = maxDelayMs;
        if (jitter > 0) {
            double factor = 1.0 + (ThreadLocalRandom.current().nextDouble() * 2 - 1) * jitter;
            delay = (long) (delay * factor);
        }
        return delay;
    }
}
