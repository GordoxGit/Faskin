package fr.heneriacore.net.exceptions;

/**
 * Thrown when circuit breaker is open and request is rejected.
 */
public class CircuitOpenException extends RuntimeException {
    public CircuitOpenException(String message) {
        super(message);
    }
}
