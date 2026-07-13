package com.fooddelivery.service;

import com.fooddelivery.dto.request.CityRequest;
import com.fooddelivery.dto.response.CityResponse;
import java.util.List;

/**
 * Manages geographic cities.
 *
 * <p>Currently admin only at the controller layer.</p>
 */
public interface CityService {

    /**
     * Find a city by id.
     */
    CityResponse getCity(Long id);

    /**
     * List all cities.
     */
    List<CityResponse> getAllCities();

    /**
     * Create a new city.
     */
    CityResponse createCity(CityRequest request);
}
