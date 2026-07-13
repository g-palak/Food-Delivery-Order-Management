package com.fooddelivery.repository;

import com.fooddelivery.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByCityId(Long cityId);
    List<Restaurant> findByOwnerId(Long ownerId);
    List<Restaurant> findByCityIdAndDeletedFalse(Long cityId);
}