package fr.heneriacore.net;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    static class MutableClock extends Clock {
        private long millis;
        MutableClock(long start) { this.millis = start; }
        void addMillis(long ms) { this.millis += ms; }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public long millis() { return millis; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis); }
    }

    @Test
    void testConsumeAndRefill() {
        MutableClock clock = new MutableClock(0);
        RateLimiter rl = new RateLimiter(60, 10, clock); // 1 token/sec
        assertTrue(rl.tryConsume(5));
        assertEquals(5, rl.getAvailableTokens());
        assertTrue(rl.tryConsume(5));
        assertFalse(rl.tryConsume());
        clock.addMillis(1000); // +1 token
        assertTrue(rl.tryConsume());
    }
}
