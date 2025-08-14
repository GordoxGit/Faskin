package com.heneria.skinview.net;

import com.heneria.skinview.metrics.MetricsCollector;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper around HttpClient adding rate limit, backoff retries and circuit breaker.
 */
public final class HttpClientWrapper {
    private final HttpClient client;
    private final RateLimiter rateLimiter;
    private final BackoffStrategy backoff;
    private final CircuitBreaker circuitBreaker;
    private final MetricsCollector metrics;
    private final ScheduledExecutorService scheduler;
    private final Logger logger;
    private final int maxRetries;
    private final Duration timeout = Duration.ofSeconds(5);
    private final String userAgent;

    public HttpClientWrapper(HttpClient client,
                             RateLimiter rateLimiter,
                             BackoffStrategy backoff,
                             CircuitBreaker circuitBreaker,
                             MetricsCollector metrics,
                             int maxRetries,
                             Logger logger,
                             String userAgent) {
        this.client = client;
        this.rateLimiter = rateLimiter;
        this.backoff = backoff;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
        this.maxRetries = Math.max(0, maxRetries);
        this.logger = logger;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "skinview-http");
            t.setDaemon(true);
            return t;
        });
        this.userAgent = userAgent;
    }

    public CompletableFuture<HttpResponse<String>> asyncGet(URI uri) {
        if (!circuitBreaker.allowRequest()) {
            metrics.incrementFailures();
            return CompletableFuture.failedFuture(new IllegalStateException("Circuit open"));
        }
        if (!rateLimiter.tryConsume()) {
            metrics.incrementThrottled();
            return CompletableFuture.failedFuture(new IllegalStateException("Rate limited"));
        }
        metrics.incrementHits();
        HttpRequest req = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(timeout)
                .header("User-Agent", userAgent)
                .build();
        return send(req, 0);
    }

    private CompletableFuture<HttpResponse<String>> send(HttpRequest req, int attempt) {
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenCompose(resp -> {
                    int sc = resp.statusCode();
                    if (sc >= 200 && sc < 300) {
                        metrics.incrementSuccesses();
                        circuitBreaker.onSuccess();
                        return CompletableFuture.completedFuture(resp);
                    }
                    if (sc >= 500 && attempt < maxRetries) {
                        metrics.incrementRetries();
                        long delay = backoff.computeDelay(attempt);
                        logger.log(Level.FINE, "Retry {0} in {1}ms", new Object[]{attempt + 1, delay});
                        CompletableFuture<HttpResponse<String>> fut = new CompletableFuture<>();
                        scheduler.schedule(() -> send(req, attempt + 1).whenComplete((r, ex) -> {
                            if (ex != null) fut.completeExceptionally(ex); else fut.complete(r);
                        }), delay, TimeUnit.MILLISECONDS);
                        return fut;
                    }
                    metrics.incrementFailures();
                    circuitBreaker.onFailure();
                    return CompletableFuture.failedFuture(new IllegalStateException("HTTP " + sc));
                })
                .exceptionallyCompose(ex -> {
                    if (attempt < maxRetries) {
                        metrics.incrementRetries();
                        long delay = backoff.computeDelay(attempt);
                        logger.log(Level.FINE, "Retry {0} in {1}ms due to {2}", new Object[]{attempt + 1, delay, ex.getMessage()});
                        CompletableFuture<HttpResponse<String>> fut = new CompletableFuture<>();
                        scheduler.schedule(() -> send(req, attempt + 1).whenComplete((r, ex2) -> {
                            if (ex2 != null) fut.completeExceptionally(ex2); else fut.complete(r);
                        }), delay, TimeUnit.MILLISECONDS);
                        return fut;
                    }
                    metrics.incrementFailures();
                    circuitBreaker.onFailure();
                    return CompletableFuture.failedFuture(ex);
                });
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
