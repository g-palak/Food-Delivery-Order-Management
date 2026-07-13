package com.fooddelivery.service;

import com.fooddelivery.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.dto.request.UpdateRestaurantRequest;
import com.fooddelivery.dto.request.RestaurantRequest;
import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import com.fooddelivery.entity.City;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.MenuItemAvailability;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.CityRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                  MenuItemRepository menuItemRepository,
                                  CityRepository cityRepository,
                                  UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RestaurantResponse getRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new DomainException("Restaurant not found: " + id));
        return toResponse(restaurant);
    }

    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new DomainException("City not found: " + request.cityId()));

        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new DomainException("Owner not found: " + request.ownerId()));

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setAddress(request.address());
        restaurant.setStatus(RestaurantStatus.OPEN);
        restaurant.setDeleted(false);
        restaurant.setCity(city);
        restaurant.setOwner(owner);

        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponse(saved);
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new DomainException("Restaurant not found: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            restaurant.setName(request.name());
        }
        if (request.address() != null && !request.address().isBlank()) {
            restaurant.setAddress(request.address());
        }
        if (request.status() != null) {
            restaurant.setStatus(request.status());
        }

        Restaurant updated = restaurantRepository.save(restaurant);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new DomainException("Restaurant not found: " + id));
        restaurant.setDeleted(true);
        restaurantRepository.save(restaurant);
    }

    @Override
    public List<RestaurantResponse> getRestaurantsByCity(Long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new DomainException("City not found: " + cityId);
        }
        return restaurantRepository.findByCityIdAndDeletedFalse(cityId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MenuItemResponse> getMenuItems(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new DomainException("Restaurant not found: " + restaurantId));

        List<MenuItem> menuItems = restaurant.getMenuItems();
        if (menuItems == null || menuItems.isEmpty()) {
            return List.of();
        }
        return menuItems.stream()
                .map(this::toMenuItemResponse)
                .toList();
    }

    @Override
    public MenuItemResponse getMenuItem(Long restaurantId, Long itemId) {
        MenuItem menuItem = menuItemRepository.findByRestaurantIdAndId(restaurantId, itemId)
                .orElseThrow(() -> new DomainException("Menu item not found: " + itemId));
        return toMenuItemResponse(menuItem);
    }

    @Override
    @Transactional
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new DomainException("Restaurant not found: " + restaurantId));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.name());
        menuItem.setPrice(request.price());
        menuItem.setStockQuantity(request.stockQuantity());
        menuItem.setAvailability(request.availability());
        menuItem.setDeleted(false);
        menuItem.setRestaurant(restaurant);

        MenuItem saved = menuItemRepository.save(menuItem);
        return toMenuItemResponse(saved);
    }

    @Override
    public MenuItemResponse updateMenuItem(Long restaurantId, Long itemId, UpdateMenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findByRestaurantIdAndId(restaurantId, itemId)
                .orElseThrow(() -> new DomainException("Menu item not found: " + itemId));

        if (request.name() != null && !request.name().isBlank()) {
            menuItem.setName(request.name());
        }
        if (request.price() != null) {
            menuItem.setPrice(request.price());
        }
        if (request.stockQuantity() != null) {
            menuItem.setStockQuantity(request.stockQuantity());
        }
        if (request.availability() != null) {
            menuItem.setAvailability(request.availability());
        }

        MenuItem updated = menuItemRepository.save(menuItem);
        return toMenuItemResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long restaurantId, Long itemId) {
        MenuItem menuItem = menuItemRepository.findByRestaurantIdAndId(restaurantId, itemId)
                .orElseThrow(() -> new DomainException("Menu item not found: " + itemId));
        menuItem.setDeleted(true);
        menuItemRepository.save(menuItem);
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getStatus(),
                restaurant.isDeleted(),
                restaurant.getCity() != null ? restaurant.getCity().getId() : null,
                restaurant.getOwner() != null ? restaurant.getOwner().getId() : null
        );
    }

    private MenuItemResponse toMenuItemResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getPrice(),
                menuItem.getStockQuantity(),
                menuItem.getAvailability(),
                menuItem.isDeleted(),
                menuItem.getRestaurant() != null ? menuItem.getRestaurant().getId() : null
        );
    }
}
