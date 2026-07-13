package com.fooddelivery.service;

import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.request.RestaurantRequest;
import com.fooddelivery.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.dto.request.UpdateRestaurantRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import java.util.List;

/**
 * Owns restaurant lifecycle and menu item lifecycle.
 *
 * <p> Admin manages restaurants.
 * Restaurant owners manage menu items under a restaurant.</p>
 */
public interface RestaurantService {

    /**
     * Find a restaurant by id, admin only.
     */
    RestaurantResponse getRestaurant(Long id);

    /**
     * List all restaurants, admin only.
     */
    List<RestaurantResponse> getAllRestaurants();

    /**
     * Create a restaurant, admin only.
     */
    RestaurantResponse createRestaurant(RestaurantRequest request);

    /**
     * Update a restaurant, admin only.
     */
    RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request);

    /**
     * Soft-delete a restaurant, admin only.
     */
    void deleteRestaurant(Long id);

    /**
     * List restaurants for a city, customer-facing browse.
     */
    List<RestaurantResponse> getRestaurantsByCity(Long cityId);

    /**
     * List menu items for a restaurant, owner-operated.
     */
    List<MenuItemResponse> getMenuItems(Long restaurantId);

    /**
     * Find a single menu item under a restaurant.
     */
    MenuItemResponse getMenuItem(Long restaurantId, Long itemId);

    /**
     * Create a menu item under a restaurant.
     */
    MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request);

    /**
     * Update a menu item under a restaurant.
     */
    MenuItemResponse updateMenuItem(Long restaurantId, Long itemId, UpdateMenuItemRequest request);

    /**
     * Remove a menu item from a restaurant.
     */
    void deleteMenuItem(Long restaurantId, Long itemId);
}
