package com.fooddelivery.entity.MenuItem;
import com.fooddelivery.enums.MenuItemAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);
    Optional<MenuItem> findByRestaurantIdAndId(Long restaurantId, Long itemId);
    boolean existsByIdAndRestaurantId(Long id, Long restaurantId);
    boolean existsByRestaurantIdAndAvailability(Long restaurantId, MenuItemAvailability availability);
    List<MenuItem> findByRestaurantIdAndAvailability(Long restaurantId, MenuItemAvailability availability);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MenuItem> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<MenuItem> findAllById(java.util.Iterable<Long> ids);
}