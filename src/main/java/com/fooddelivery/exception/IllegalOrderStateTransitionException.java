package com.fooddelivery.exception;

/**
 * Thrown when an order state transition is not allowed.
 */
public class IllegalOrderStateTransitionException extends DomainException {
    public IllegalOrderStateTransitionException(Long orderId, com.fooddelivery.enums.OrderStatus current, com.fooddelivery.enums.OrderStatus target) {
        super("Order " + orderId + " cannot transition from " + current + " to " + target);
    }
}
