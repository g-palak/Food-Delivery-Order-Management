package com.fooddelivery.exception;

/**
 * Thrown when a requested customer does not exist or has been soft-deleted.
 */
public class CustomerNotFoundException extends DomainException {
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found: " + customerId);
    }
}
