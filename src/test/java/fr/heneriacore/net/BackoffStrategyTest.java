package fr.heneriacore.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackoffStrategyTest {
    @Test
    void testDelays() {
        BackoffStrategy b = new BackoffStrategy(100, 1000, 0.0);
        assertEquals(100, b.nextDelayMs(0));
        assertEquals(200, b.nextDelayMs(1));
        assertEquals(800, b.nextDelayMs(3));
        assertEquals(1000, b.nextDelayMs(10)); // clamped
    }

    @Test
    void testJitterRange() {
        BackoffStrategy b = new BackoffStrategy(100, 1000, 0.2); // 20% jitter
        long delay = b.nextDelayMs(2); // base 400
        assertTrue(delay >= 320 && delay <= 480);
    }
}
