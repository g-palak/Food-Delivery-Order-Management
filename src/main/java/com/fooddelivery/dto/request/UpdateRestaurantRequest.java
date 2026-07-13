package com.fooddelivery.dto.request;

import com.fooddelivery.enums.RestaurantStatus;
import jakarta.validation.constraints.Size;

public record UpdateRestaurantRequest(
        @Size(max = 150)
        String name,

        @Size(max = 255)
        String address,

        RestaurantStatus status
) {
}