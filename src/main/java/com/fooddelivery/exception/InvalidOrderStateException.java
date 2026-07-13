package com.fooddelivery.exception;

/**
 * Thrown when an order state transition is not allowed.
 */
public class InvalidOrderStateException extends DomainException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
