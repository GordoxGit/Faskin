package com.heneria.skinview.net;

import com.heneria.skinview.metrics.MetricsCollector;

/**
 * Lightweight circuit breaker with CLOSED → OPEN → HALF_OPEN states.
 */
public final class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final long openDurationMs;
    private final int halfOpenProbeCount;
    private final MetricsCollector metrics;

    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private long openSince = 0L;
    private int probeRemaining = 0;
    private int probeSuccess = 0;

    public CircuitBreaker(int failureThreshold, long openDurationMs, int halfOpenProbeCount, MetricsCollector metrics) {
        this.failureThreshold = Math.max(1, failureThreshold);
        this.openDurationMs = Math.max(1, openDurationMs);
        this.halfOpenProbeCount = Math.max(1, halfOpenProbeCount);
        this.metrics = metrics;
        metrics.setCircuitState(state);
    }

    /** Should a request be attempted? */
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        if (state == State.OPEN) {
            if (now - openSince >= openDurationMs) {
                state = State.HALF_OPEN;
                probeRemaining = halfOpenProbeCount;
                probeSuccess = 0;
                metrics.setCircuitState(state);
            } else {
                return false;
            }
        }
        if (state == State.HALF_OPEN) {
            if (probeRemaining <= 0) return false;
            probeRemaining--;
        }
        return true;
    }

    public synchronized void onSuccess() {
        consecutiveFailures = 0;
        if (state == State.HALF_OPEN) {
            probeSuccess++;
            if (probeSuccess >= halfOpenProbeCount) close();
        }
    }

    public synchronized void onFailure() {
        metrics.recordFailureTime();
        if (state == State.HALF_OPEN) {
            open();
            return;
        }
        consecutiveFailures++;
        if (consecutiveFailures >= failureThreshold) open();
    }

    private void open() {
        state = State.OPEN;
        openSince = System.currentTimeMillis();
        consecutiveFailures = 0;
        metrics.setCircuitState(state);
    }

    private void close() {
        state = State.CLOSED;
        consecutiveFailures = 0;
        metrics.setCircuitState(state);
    }

    public synchronized State state() { return state; }
}
