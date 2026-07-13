package com.fooddelivery.dto.response;

import java.math.BigDecimal;

public record CityResponse(
        Long id,
        String name,
        String country,
        String state,
        BigDecimal longitude,
        BigDecimal latitude
) {
}