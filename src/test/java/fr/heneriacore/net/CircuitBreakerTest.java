package fr.heneriacore.net;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {
    static class MutableClock extends Clock {
        private long ms;
        MutableClock(long start) { this.ms = start; }
        void add(long delta) { ms += delta; }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public long millis() { return ms; }
        @Override public Instant instant() { return Instant.ofEpochMilli(ms); }
    }

    @Test
    void testTransitions() {
        MutableClock clock = new MutableClock(0);
        CircuitBreaker cb = new CircuitBreaker(2, 1000, 1, clock);
        assertTrue(cb.allowRequest());
        cb.onFailure();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.onFailure();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertFalse(cb.allowRequest());
        clock.add(1000);
        assertTrue(cb.allowRequest()); // HALF_OPEN probe
        cb.onSuccess();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }
}
