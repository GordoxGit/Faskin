package com.faskin.auth.premium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public final class PremiumMetrics {
    private final LongAdder bypassOk = new LongAdder();
    private final LongAdder bypassRefused = new LongAdder();
    private final LongAdder refusedForwarding = new LongAdder();
    private final LongAdder refusedNoTextures = new LongAdder();
    private final LongAdder refusedFallback = new LongAdder();
    private final List<Long> preAuthTimes = Collections.synchronizedList(new ArrayList<>());

    public void recordPreAuth(long ms) { preAuthTimes.add(ms); }

    public void recordEvaluation(PremiumEvaluation eval) {
        switch (eval) {
            case PREMIUM_SAFE -> bypassOk.increment();
            case NOT_PREMIUM_FORWARDING_MISSING -> {
                bypassRefused.increment();
                refusedForwarding.increment();
            }
            case NOT_PREMIUM_NO_TEXTURES -> {
                bypassRefused.increment();
                refusedNoTextures.increment();
            }
            case NOT_PREMIUM_FALLBACK_MODE -> {
                bypassRefused.increment();
                refusedFallback.increment();
            }
            case NOT_PREMIUM_UNKNOWN -> bypassRefused.increment();
            default -> {}
        }
    }

    public long bypassOkTotal() { return bypassOk.sum(); }
    public long bypassRefusedTotal() { return bypassRefused.sum(); }
    public long bypassRefusedForwarding() { return refusedForwarding.sum(); }
    public long bypassRefusedNoTextures() { return refusedNoTextures.sum(); }
    public long bypassRefusedFallbackMode() { return refusedFallback.sum(); }

    public long preAuthMsAvg() {
        if (preAuthTimes.isEmpty()) return 0L;
        long sum = 0L;
        for (long l : preAuthTimes) sum += l;
        return sum / preAuthTimes.size();
    }

    public long preAuthMsP95() {
        if (preAuthTimes.isEmpty()) return 0L;
        List<Long> copy = new ArrayList<>(preAuthTimes);
        Collections.sort(copy);
        int idx = (int) Math.ceil(copy.size() * 0.95) - 1;
        if (idx < 0) idx = 0;
        return copy.get(idx);
    }
}
