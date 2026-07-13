package com.fooddelivery.service;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.OrderItem;
import com.fooddelivery.enums.MenuItemAvailability;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates all stock-facing operations required before/during/after order placement.
 *
 * <p>Why this exists separately from OrderService:
 * keeping inventory work in one place prevents duplicated pessimistic-lock logic
 * across controllers or other services.</p>
 */
@Service
public class InventoryService {

    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;

    public InventoryService(MenuItemRepository menuItemRepository,
                            OrderItemRepository orderItemRepository) {
        this.menuItemRepository = menuItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Validate that requested menu items exist, are available, and have enough stock.
     *
     * <p>Read-only; no transaction boundaries here because it only reads.</p>
     */
    public void validateStock(PlaceOrderRequest request) {
        List<MenuItem> menuItems = menuItemRepository.findAllById(
                request.items().stream().map(PlaceOrderRequest.ItemRequest::menuItemId).toList()
        );

        Map<Long, MenuItem> itemMap = new HashMap<>();
        for (MenuItem item : menuItems) {
            itemMap.put(item.getId(), item);
        }

        for (PlaceOrderRequest.ItemRequest itemReq : request.items()) {
            MenuItem item = itemMap.get(itemReq.menuItemId());

            if (item == null) {
                throw new RuntimeException("Menu item not found: " + itemReq.menuItemId());
            }
            if (item.isDeleted()) {
                throw new RuntimeException("Menu item unavailable: " + itemReq.menuItemId());
            }
            if (item.getAvailability() != MenuItemAvailability.AVAILABLE) {
                throw new RuntimeException("Menu item not available: " + itemReq.menuItemId());
            }
            if (item.getStockQuantity() < itemReq.quantity()) {
                throw new RuntimeException("Insufficient stock for: " + itemReq.menuItemId());
            }
        }
    }

    /**
     * Reserve stock by deducting quantity under pessimistic lock.
     *
     * <p>Requires the caller to open a transaction boundary.</p>
     */
    @Transactional
    public void reserveStock(PlaceOrderRequest request) {
        List<MenuItem> lockedItems = menuItemRepository.findAllById(
                request.items().stream().map(PlaceOrderRequest.ItemRequest::menuItemId).toList()
        );

        Map<Long, MenuItem> lockedMap = new HashMap<>();
        for (MenuItem item : lockedItems) {
            lockedMap.put(item.getId(), item);
        }

        for (PlaceOrderRequest.ItemRequest itemReq : request.items()) {
            MenuItem item = lockedMap.get(itemReq.menuItemId());
            item.setStockQuantity(item.getStockQuantity() - itemReq.quantity());
            menuItemRepository.save(item);
        }
    }

    /**
     * Release previously reserved stock back to inventory.
     *
     * <p>Used when payment fails or order is cancelled before delivery.</p>
     */
    @Transactional
    public void releaseStock(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        List<Long> menuItemIds = orderItems.stream()
                .map(item -> item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .toList();

        List<MenuItem> items = menuItemRepository.findAllById(menuItemIds);
        Map<Long, MenuItem> itemMap = new HashMap<>();
        for (MenuItem item : items) {
            itemMap.put(item.getId(), item);
        }

        for (OrderItem orderItem : orderItems) {
            Long menuItemId = orderItem.getMenuItem() != null ? orderItem.getMenuItem().getId() : null;
            if (menuItemId == null) {
                continue;
            }
            MenuItem item = itemMap.get(menuItemId);
            if (item == null) {
                continue;
            }
            item.setStockQuantity(item.getStockQuantity() + orderItem.getQuantity());
            menuItemRepository.save(item);
        }
    }

    /**
     * Permanently deduct stock after successful delivery.
     */
    @Transactional
    public void deductStock(List<OrderItem> orderItems) {
        releaseStock(orderItems);
    }

    /**
     * Ensure menu items referenced by an order still exist and have stock.
     */
    public boolean hasStock(Long menuItemId, int requiredQuantity) {
        return menuItemRepository.findById(menuItemId)
                .map(item -> !item.isDeleted()
                        && item.getAvailability() == MenuItemAvailability.AVAILABLE
                        && item.getStockQuantity() >= requiredQuantity)
                .orElse(false);
    }
}
