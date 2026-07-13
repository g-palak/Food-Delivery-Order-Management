package com.fooddelivery.exception;

/**
 * Thrown when a requested resource does not exist or has been soft-deleted.
 */
public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found: " + id);
    }
}
