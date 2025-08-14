package fr.heneriacore.net;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token bucket rate limiter.
 */
public class RateLimiter {
    private final int capacity;
    private final long refillTokensPerMs;
    private final Clock clock;
    private final AtomicLong availableTokens;
    private final AtomicLong lastRefillMs;

    public RateLimiter(int requestsPerMinute, int burst, Clock clock) {
        if (requestsPerMinute <= 0 || burst <= 0) {
            throw new IllegalArgumentException("rpm and burst must be > 0");
        }
        this.capacity = burst;
        this.refillTokensPerMs = requestsPerMinute * 1000L / 60000L; // tokens per ms scaled by 1000
        this.clock = clock;
        this.availableTokens = new AtomicLong(burst);
        this.lastRefillMs = new AtomicLong(clock.millis());
    }

    public RateLimiter(int requestsPerMinute, int burst) {
        this(requestsPerMinute, burst, Clock.systemUTC());
    }

    /**
     * Try to consume tokens from the bucket.
     *
     * @return true if enough tokens were available
     */
    public boolean tryConsume(int tokens) {
        refill();
        while (true) {
            long cur = availableTokens.get();
            if (cur < tokens) return false;
            if (availableTokens.compareAndSet(cur, cur - tokens)) return true;
        }
    }

    public boolean tryConsume() {
        return tryConsume(1);
    }

    /**
     * @return current available tokens
     */
    public int getAvailableTokens() {
        refill();
        return (int) Math.min(Integer.MAX_VALUE, availableTokens.get());
    }

    private void refill() {
        long now = clock.millis();
        long last = lastRefillMs.get();
        long elapsed = now - last;
        if (elapsed <= 0) return;
        long tokensToAdd = elapsed * refillTokensPerMs / 1000L;
        if (tokensToAdd <= 0) return;
        long newTimestamp = last + elapsed;
        if (lastRefillMs.compareAndSet(last, newTimestamp)) {
            availableTokens.updateAndGet(cur -> Math.min(capacity, cur + tokensToAdd));
        }
    }
}

