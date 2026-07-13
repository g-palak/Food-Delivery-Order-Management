package com.fooddelivery.dto.response;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        Long restaurantId,
        OrderStatus orderStatus,
        BigDecimal totalPrice,
        PaymentStatus paymentStatus,
        String paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime paymentDoneAt,
        List<OrderItemResponse> items
) {
}