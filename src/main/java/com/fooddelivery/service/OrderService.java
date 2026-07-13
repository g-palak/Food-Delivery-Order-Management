package com.fooddelivery.service;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import java.util.List;

/**
 * Order lifecycle and state transitions.
 *
 * <p>State machine is intentionally top-down:
 * PLACED -> ACCEPTED -> PREPARING -> READY -> PICKED_UP -> DELIVERED.
 * Business rules that shall be enforced in implementation:
 * only allowed transitions are valid,
 * total price comes from snapshot menu data, not mutable menu item,
 * customer cancellation is limited by current order phase.</p>
 */
public interface OrderService {

    /**
     * Find order by id.
     */
    OrderResponse getOrder(Long id);

    /**
     * Place an order.
     *
     * <p>Transaction boundary should start here.
     * Save order and order items,
     * deduct inventory with pessimistic locking,
     * publish placement event for async assignment notification.</p>
     */
    OrderResponse placeOrder(Long customerId, PlaceOrderRequest request);

    /**
     * Restaurant accepts order.
     */
    OrderResponse acceptOrder(Long id);

    /**
     * Restaurant rejects order.
     */
    OrderResponse rejectOrder(Long id);

    /**
     * Restaurant marks order as preparing.
     */
    OrderResponse preparingOrder(Long id);

    /**
     * Restaurant marks order ready for delivery.
     */
    OrderResponse readyOrder(Long id);

    /**
     * Delivery partner picks up order.
     */
    OrderResponse pickupOrder(Long id);

    /**
     * Delivery partner delivers order.
     */
    OrderResponse deliverOrder(Long id);

    /**
     * Customer cancels order when still allowed.
     */
    OrderResponse cancelOrder(Long id);

    /**
     * Order history for a specific customer.
     */
    List<OrderResponse> getCustomerOrders(Long customerId);

    /**
     * Order history for the currently authenticated customer.
     */
    List<OrderResponse> getMyOrders();
}
