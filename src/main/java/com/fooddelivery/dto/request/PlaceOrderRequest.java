package com.fooddelivery.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceOrderRequest(
        @NotNull(message = "Restaurant ID is required")
        Long restaurantId,

        @NotNull(message = "Order items are required")
        @Size(min = 1, message = "Order must contain at least one item")
        List<OrderItemRequest> items
) {
}