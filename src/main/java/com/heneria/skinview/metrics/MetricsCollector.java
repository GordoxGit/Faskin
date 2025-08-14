package com.heneria.skinview.metrics;

import com.heneria.skinview.net.CircuitBreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe metrics collector for Mojang requests.
 */
public final class MetricsCollector {
    private final LongAdder hits = new LongAdder();
    private final LongAdder successes = new LongAdder();
    private final LongAdder failures = new LongAdder();
    private final LongAdder retries = new LongAdder();
    private final LongAdder throttled = new LongAdder();
    private final AtomicLong lastFailureTimestamp = new AtomicLong(0L);
    private volatile CircuitBreaker.State circuitState = CircuitBreaker.State.CLOSED;

    public void incrementHits() { hits.increment(); }
    public void incrementSuccesses() { successes.increment(); }
    public void incrementFailures() { failures.increment(); }
    public void incrementRetries() { retries.increment(); }
    public void incrementThrottled() { throttled.increment(); }
    public void recordFailureTime() { lastFailureTimestamp.set(System.currentTimeMillis()); }

    public void setCircuitState(CircuitBreaker.State state) { this.circuitState = state; }

    public long hits() { return hits.longValue(); }
    public long successes() { return successes.longValue(); }
    public long failures() { return failures.longValue(); }
    public long retries() { return retries.longValue(); }
    public long throttled() { return throttled.longValue(); }
    public CircuitBreaker.State circuitState() { return circuitState; }
    public long lastFailureTimestamp() { return lastFailureTimestamp.get(); }

    public Map<String, Long> snapshot() {
        Map<String, Long> m = new ConcurrentHashMap<>();
        m.put("hits", hits());
        m.put("successes", successes());
        m.put("failures", failures());
        m.put("retries", retries());
        m.put("throttled", throttled());
        m.put("lastFailure", lastFailureTimestamp());
        return m;
    }
}
