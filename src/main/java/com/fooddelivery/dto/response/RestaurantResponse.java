package com.fooddelivery.dto.response;

import com.fooddelivery.enums.RestaurantStatus;
import java.math.BigDecimal;

public record RestaurantResponse(
        Long id,
        String name,
        String address,
        RestaurantStatus status,
        boolean deleted,
        Long cityId,
        Long ownerId
) {
}