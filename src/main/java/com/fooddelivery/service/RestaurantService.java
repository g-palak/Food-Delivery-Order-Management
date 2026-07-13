package com.fooddelivery.service;

import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.request.RestaurantRequest;
import com.fooddelivery.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.dto.request.UpdateRestaurantRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import java.util.List;

public interface RestaurantService {
    RestaurantResponse getRestaurant(Long id);
    List<RestaurantResponse> getAllRestaurants();
    RestaurantResponse createRestaurant(RestaurantRequest request);
    RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request);
    void deleteRestaurant(Long id);
    List<RestaurantResponse> getRestaurantsByCity(Long cityId);
    List<MenuItemResponse> getMenuItems(Long restaurantId);
    MenuItemResponse getMenuItem(Long restaurantId, Long itemId);
    MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request);
    MenuItemResponse updateMenuItem(Long restaurantId, Long itemId, UpdateMenuItemRequest request);
    void deleteMenuItem(Long restaurantId, Long itemId);
}