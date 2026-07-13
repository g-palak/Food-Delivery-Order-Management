package com.fooddelivery.exception;

/**
 * Thrown when a create/update operation would violate a uniqueness or business rule.
 */
public class DuplicateResourceException extends DomainException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
