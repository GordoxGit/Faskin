package fr.heneriacore.net;

import fr.heneriacore.metrics.MetricsCollector;
import fr.heneriacore.net.exceptions.CircuitOpenException;
import fr.heneriacore.net.exceptions.RateLimitException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around HttpClient providing rate limiting, retries and circuit breaker.
 */
public class HttpClientWrapper {
    private final HttpClient client;
    private final RateLimiter rateLimiter;
    private final BackoffStrategy backoff;
    private final CircuitBreaker circuitBreaker;
    private final MetricsCollector metrics;
    private final ScheduledExecutorService scheduler;
    private final int maxRetries;

    public HttpClientWrapper(HttpClient client,
                             RateLimiter rateLimiter,
                             BackoffStrategy backoff,
                             CircuitBreaker circuitBreaker,
                             MetricsCollector metrics,
                             RetryPolicy policy,
                             ScheduledExecutorService scheduler) {
        this.client = client;
        this.rateLimiter = rateLimiter;
        this.backoff = backoff;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
        this.scheduler = scheduler;
        this.maxRetries = policy.getMaxRetries();
    }

    public CompletableFuture<HttpResponse<String>> getAsync(URI uri, Map<String, String> headers) {
        metrics.incHits();
        metrics.setCircuitState(circuitBreaker.getState().name());
        if (!circuitBreaker.allowRequest()) {
            metrics.setCircuitState(circuitBreaker.getState().name());
            return CompletableFuture.failedFuture(new CircuitOpenException("circuit open"));
        }
        if (!rateLimiter.tryConsume()) {
            metrics.incThrottled();
            return CompletableFuture.failedFuture(new RateLimitException("rate limit exceeded"));
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).GET();
        if (headers != null) headers.forEach(builder::header);
        HttpRequest request = builder.build();
        CompletableFuture<HttpResponse<String>> result = new CompletableFuture<>();
        send(request, 0, result);
        return result;
    }

    private void send(HttpRequest request, int attempt, CompletableFuture<HttpResponse<String>> result) {
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((resp, ex) -> {
                    if (ex == null && resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        metrics.incSuccess();
                        circuitBreaker.onSuccess();
                        metrics.setCircuitState(circuitBreaker.getState().name());
                        result.complete(resp);
                    } else if (ex == null && resp.statusCode() >= 400 && resp.statusCode() < 500) {
                        metrics.incSuccess();
                        circuitBreaker.onSuccess();
                        metrics.setCircuitState(circuitBreaker.getState().name());
                        result.complete(resp);
                    } else {
                        if (attempt < maxRetries) {
                            metrics.incRetry();
                            long delay = backoff.nextDelayMs(attempt);
                            scheduler.schedule(() -> send(request, attempt + 1, result), delay, TimeUnit.MILLISECONDS);
                        } else {
                            metrics.incFailure();
                            circuitBreaker.onFailure();
                            metrics.setCircuitState(circuitBreaker.getState().name());
                            Throwable cause = ex != null ? ex : new RuntimeException("HTTP " + (resp != null ? resp.statusCode() : "error"));
                            result.completeExceptionally(cause);
                        }
                    }
                });
    }

    public CompletableFuture<String> getJsonAsync(URI uri, Map<String, String> headers) {
        return getAsync(uri, headers).thenApply(HttpResponse::body);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
