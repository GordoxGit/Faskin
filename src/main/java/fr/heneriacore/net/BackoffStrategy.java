package fr.heneriacore.net;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Exponential backoff with optional jitter.
 */
public class BackoffStrategy {
    private final long baseDelayMs;
    private final long maxDelayMs;
    private final double jitter;

    public BackoffStrategy(long baseDelayMs, long maxDelayMs, double jitter) {
        this.baseDelayMs = baseDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.jitter = jitter;
    }

    public long nextDelayMs(int retryCount) {
        long delay = baseDelayMs * (1L << retryCount);
        if (delay < 0) delay = Long.MAX_VALUE;
        if (jitter > 0d) {
            long delta = (long) (delay * jitter);
            long min = Math.max(0L, delay - delta);
            long max = delay + delta;
            delay = ThreadLocalRandom.current().nextLong(min, max + 1);
        }
        if (delay > maxDelayMs) delay = maxDelayMs;
        return delay;
    }
}
