package com.fooddelivery.exception;

/**
 * Thrown when an operation cannot proceed because stock is insufficient.
 */
public class InsufficientInventoryException extends DomainException {
    public InsufficientInventoryException(String message) {
        super(message);
    }
}
