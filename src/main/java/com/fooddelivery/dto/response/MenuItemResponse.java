package com.fooddelivery.dto.response;

import com.fooddelivery.enums.MenuItemAvailability;
import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        MenuItemAvailability availability,
        boolean deleted,
        Long restaurantId
) {
}