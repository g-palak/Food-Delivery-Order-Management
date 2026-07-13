package com.fooddelivery.service;

import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.enums.OrderStatus;

/**
 * Simulates payment processing against orders.
 *
 * <p>Responsibilities:
 * validate order state, simulate payment attempt,
 * update payment status, and trigger downstream effects.</p>
 */
public interface PaymentService {

    /**
     * Simulate payment for an order.
     *
     * <p>Expected flow: called during order creation while
     * the order state is still mutable. If payment succeeds,
     * the order transitions onward; if it fails, order placement
     * should be rolled back or marked failed.</p>
     */
    PaymentStatus processPayment(Long orderId);

    /**
     * Confirm a pending payment as successful.
     *
     * <p>Implementation should verify the order is still in PAYMENT_PENDING
     * and not already expired or cancelled.</p>
     PaymentStatus markPaymentSuccess(Long orderId);

    /**
     * Mark payment as failed.
     *
     * <p>Used when simulated payment attempt fails or external timeout occurs.</p>
     */
    PaymentStatus markPaymentFailed(Long orderId);

    /**
     * Refund an order payment.
     *
     * <p>Only valid when payment was previously successful
     * and the order is in a refundable terminal state.</p>
     */
    PaymentStatus refundPayment(Long orderId);
}
