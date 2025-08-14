package fr.heneriacore.net;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple circuit breaker with CLOSED, OPEN and HALF_OPEN states.
 */
public class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final long openDurationMs;
    private final int halfOpenProbeCount;
    private final Clock clock;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private volatile long openTimestamp;
    private volatile int halfOpenAttempts;

    public CircuitBreaker(int failureThreshold, long openDurationMs, int halfOpenProbeCount, Clock clock) {
        this.failureThreshold = failureThreshold;
        this.openDurationMs = openDurationMs;
        this.halfOpenProbeCount = halfOpenProbeCount;
        this.clock = clock;
    }

    public CircuitBreaker(int failureThreshold, long openDurationMs, int halfOpenProbeCount) {
        this(failureThreshold, openDurationMs, halfOpenProbeCount, Clock.systemUTC());
    }

    public boolean allowRequest() {
        State s = state.get();
        if (s == State.CLOSED) return true;
        long now = clock.millis();
        if (s == State.OPEN) {
            if (now - openTimestamp >= openDurationMs) {
                state.set(State.HALF_OPEN);
                halfOpenAttempts = 0;
                return true;
            }
            return false;
        }
        // HALF_OPEN
        if (halfOpenAttempts < halfOpenProbeCount) {
            halfOpenAttempts++;
            return true;
        }
        return false;
    }

    public void onSuccess() {
        if (state.get() != State.CLOSED) {
            state.set(State.CLOSED);
        }
        consecutiveFailures.set(0);
    }

    public void onFailure() {
        if (state.get() == State.CLOSED) {
            int fail = consecutiveFailures.incrementAndGet();
            if (fail >= failureThreshold) {
                state.set(State.OPEN);
                openTimestamp = clock.millis();
            }
        } else if (state.get() == State.HALF_OPEN) {
            state.set(State.OPEN);
            openTimestamp = clock.millis();
        }
    }

    public State getState() {
        State s = state.get();
        if (s == State.OPEN && clock.millis() - openTimestamp >= openDurationMs) {
            state.compareAndSet(State.OPEN, State.HALF_OPEN);
            s = state.get();
        }
        return s;
    }
}
