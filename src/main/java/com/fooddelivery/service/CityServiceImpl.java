package com.fooddelivery.service;

import com.fooddelivery.dto.request.CityRequest;
import com.fooddelivery.dto.response.CityResponse;
import com.fooddelivery.entity.City;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public CityResponse getCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new DomainException("City not found: " + id));
        return toResponse(city);
    }

    @Override
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CityResponse createCity(CityRequest request) {
        City city = new City();
        city.setName(request.name());
        city.setCountry(request.country());
        city.setState(request.state());
        city.setLongitude(request.longitude());
        city.setLatitude(request.latitude());

        City saved = cityRepository.save(city);
        return toResponse(saved);
    }

    private CityResponse toResponse(City city) {
        return new CityResponse(
                city.getId(),
                city.getName(),
                city.getCountry(),
                city.getState(),
                city.getLongitude(),
                city.getLatitude()
        );
    }
}
