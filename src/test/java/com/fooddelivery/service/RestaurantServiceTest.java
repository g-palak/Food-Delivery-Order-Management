package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.request.RestaurantRequest;
import com.fooddelivery.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.dto.request.UpdateRestaurantRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import com.fooddelivery.entity.City;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.MenuItemAvailability;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.CityRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    RestaurantRepository restaurantRepository;
    @Mock
    MenuItemRepository menuItemRepository;
    @Mock
    CityRepository cityRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    RestaurantServiceImpl restaurantService;

    @Test
    void createRestaurant_success_returnsResponse() {
        City city = new City();
        city.setId(1L);

        User owner = new User();
        owner.setId(2L);

        RestaurantRequest request = new RestaurantRequest("Tasty", "123 Main", 1L, 2L);
        when(cityRepository.findById(1L)).thenReturn(java.util.Optional.of(city));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(owner));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> {
            Restaurant r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        RestaurantResponse response = restaurantService.createRestaurant(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Tasty", response.name());
    }

    @Test
    void createMenuItem_success_returnsResponse() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        MenuItemRequest request = new MenuItemRequest("Burger", new BigDecimal("50"), 20, MenuItemAvailability.AVAILABLE);
        when(restaurantRepository.findById(1L)).thenReturn(java.util.Optional.of(restaurant));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(i -> {
            MenuItem item = i.getArgument(0);
            item.setId(100L);
            return item;
        });

        MenuItemResponse response = restaurantService.createMenuItem(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Burger", response.name());
    }

    @Test
    void deleteMenuItem_softDeletesItem() {
        MenuItem item = new MenuItem();
        item.setId(100L);
        item.setDeleted(false);

        when(menuItemRepository.findByRestaurantIdAndId(1L, 100L)).thenReturn(java.util.Optional.of(item));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        restaurantService.deleteMenuItem(1L, 100L);

        assertEquals(true, item.isDeleted());
        verify(menuItemRepository).save(item);
    }
}
