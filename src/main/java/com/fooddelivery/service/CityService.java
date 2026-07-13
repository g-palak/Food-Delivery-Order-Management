package com.fooddelivery.service;

import com.fooddelivery.dto.request.CityRequest;
import com.fooddelivery.dto.response.CityResponse;
import java.util.List;

public interface CityService {
    CityResponse getCity(Long id);
    List<CityResponse> getAllCities();
    CityResponse createCity(CityRequest request);
}