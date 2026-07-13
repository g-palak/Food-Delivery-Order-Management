package com.fooddelivery.event;

import com.fooddelivery.entity.Order;
import java.time.LocalDateTime;

/**
 * Published after an order is successfully placed and committed.
 */
public class OrderPlacedEvent {

    private final Order order;
    private final LocalDateTime occurredAt;

    public OrderPlacedEvent(Order order) {
        this.order = order;
        this.occurredAt = LocalDateTime.now();
    }

    public Order getOrder() {
        return order;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
