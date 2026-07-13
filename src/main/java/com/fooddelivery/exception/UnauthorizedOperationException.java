package com.fooddelivery.exception;

/**
 * Thrown when the authenticated principal is not allowed to perform an operation.
 */
public class UnauthorizedOperationException extends DomainException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
