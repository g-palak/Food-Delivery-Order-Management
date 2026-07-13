package com.fooddelivery.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long menuItemId,
        String menuItemName,
        Integer quantity,
        BigDecimal price
) {
}