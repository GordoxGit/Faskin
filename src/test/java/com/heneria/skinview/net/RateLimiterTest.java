package com.heneria.skinview.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    @Test
    void testConsumeAndRefill() throws InterruptedException {
        RateLimiter rl = new RateLimiter(60, 2); // 1 token/sec, burst 2
        assertTrue(rl.tryConsume());
        assertTrue(rl.tryConsume());
        assertFalse(rl.tryConsume());
        Thread.sleep(1100);
        assertTrue(rl.tryConsume());
    }
}
