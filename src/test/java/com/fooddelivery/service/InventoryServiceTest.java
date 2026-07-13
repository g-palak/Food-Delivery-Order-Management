package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OrderItem;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.OrderItemRepository;
import com.fooddelivery.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    MenuItemRepository menuItemRepository;
    @Mock
    OrderItemRepository orderItemRepository;

    @InjectMocks
    InventoryService inventoryService;

    @Test
    void hasStock_returnsTrue_whenStockSufficient() {
        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setDeleted(false);
        item.setAvailability(com.fooddelivery.enums.MenuItemAvailability.AVAILABLE);
        item.setStockQuantity(10);

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertEquals(true, inventoryService.hasStock(1L, 5));
    }

    @Test
    void hasStock_returnsFalse_whenInsufficientStock() {
        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setDeleted(false);
        item.setAvailability(com.fooddelivery.enums.MenuItemAvailability.AVAILABLE);
        item.setStockQuantity(2);

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertEquals(false, inventoryService.hasStock(1L, 5));
    }

    @Test
    void reserveStock_reducesStockQuantities() {
        MenuItem item1 = new MenuItem();
        item1.setId(1L);
        item1.setStockQuantity(10);

        MenuItem item2 = new MenuItem();
        item2.setId(2L);
        item2.setStockQuantity(5);

        PlaceOrderRequest request = new PlaceOrderRequest(1L, List.of(
                new PlaceOrderRequest.ItemRequest(1L, 2),
                new PlaceOrderRequest.ItemRequest(2L, 1)
        ));

        when(menuItemRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(item1, item2));

        inventoryService.reserveStock(request);

        assertEquals(8, item1.getStockQuantity());
        assertEquals(4, item2.getStockQuantity());
    }
}
