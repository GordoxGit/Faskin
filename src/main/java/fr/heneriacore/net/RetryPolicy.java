package fr.heneriacore.net;

/**
 * Immutable configuration for retry behavior.
 */
public class RetryPolicy {
    private final int maxRetries;
    private final long baseDelayMs;
    private final long maxDelayMs;
    private final double jitter;

    public RetryPolicy(int maxRetries, long baseDelayMs, long maxDelayMs, double jitter) {
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.jitter = jitter;
    }

    public int getMaxRetries() { return maxRetries; }
    public long getBaseDelayMs() { return baseDelayMs; }
    public long getMaxDelayMs() { return maxDelayMs; }
    public double getJitter() { return jitter; }
}
