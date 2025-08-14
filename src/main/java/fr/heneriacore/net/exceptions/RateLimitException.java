package fr.heneriacore.net.exceptions;

/**
 * Thrown when the rate limiter rejects a request.
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
