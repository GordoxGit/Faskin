package fr.heneriacore.metrics;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Simple metrics collector for Mojang API calls.
 */
public class MetricsCollector {
    private final LongAdder hits = new LongAdder();
    private final LongAdder successes = new LongAdder();
    private final LongAdder failures = new LongAdder();
    private final LongAdder retries = new LongAdder();
    private final LongAdder throttled = new LongAdder();
    private final AtomicReference<String> circuitState = new AtomicReference<>("CLOSED");
    private final AtomicLong lastFailureTs = new AtomicLong(0);
    private final Clock clock;

    public MetricsCollector(Clock clock) {
        this.clock = clock;
    }

    public MetricsCollector() { this(Clock.systemUTC()); }

    public void incHits() { hits.increment(); }
    public void incSuccess() { successes.increment(); }
    public void incFailure() {
        failures.increment();
        lastFailureTs.set(clock.millis());
    }
    public void incRetry() { retries.increment(); }
    public void incThrottled() { throttled.increment(); }

    public void setCircuitState(String state) { circuitState.set(state); }

    public Map<String, Object> snapshot() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mojang.hits", hits.sum());
        map.put("mojang.successes", successes.sum());
        map.put("mojang.failures", failures.sum());
        map.put("mojang.retries", retries.sum());
        map.put("mojang.throttled", throttled.sum());
        map.put("mojang.circuit_state", circuitState.get());
        map.put("mojang.last_failure_ts", lastFailureTs.get());
        return map;
    }
}
