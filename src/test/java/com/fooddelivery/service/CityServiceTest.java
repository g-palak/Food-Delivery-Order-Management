package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.request.CityRequest;
import com.fooddelivery.dto.response.CityResponse;
import com.fooddelivery.entity.City;
import com.fooddelivery.repository.CityRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    CityRepository cityRepository;

    @InjectMocks
    CityServiceImpl cityService;

    @Test
    void createCity_success_returnsResponse() {
        CityRequest request = new CityRequest(
                "New York",
                "USA",
                "NY",
                new BigDecimal("-74.0"),
                new BigDecimal("40.7")
        );

        when(cityRepository.save(any(City.class))).thenAnswer(i -> {
            City c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        CityResponse response = cityService.createCity(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("New York", response.name());
    }

    @Test
    void getAllCities_returnsAllMapped() {
        City city = new City();
        city.setId(1L);
        city.setName("NYC");

        when(cityRepository.findAll()).thenReturn(List.of(city));

        List<CityResponse> cities = cityService.getAllCities();

        assertEquals(1, cities.size());
        assertEquals("NYC", cities.get(0).name());
    }
}
