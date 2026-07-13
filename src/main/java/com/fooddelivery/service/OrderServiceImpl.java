package com.fooddelivery.service;

import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderItemResponse;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.entity.*;
import com.fooddelivery.enums.*;
import com.fooddelivery.event.OrderPlacedEvent;
import com.fooddelivery.repository.*;
import com.fooddelivery.service.PaymentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            UserRepository userRepository,
                            RestaurantRepository restaurantRepository,
                            MenuItemRepository menuItemRepository,
                            PaymentService paymentService,
                            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    @Transactional
    public synchronized OrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {
        // 1. Validate customer
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 2. Validate restaurant
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (restaurant.getStatus() != RestaurantStatus.OPEN) {
            throw new RuntimeException("Restaurant is not accepting orders");
        }
        if (restaurant.isDeleted()) {
            throw new RuntimeException("Restaurant is unavailable");
        }

        // 3. Validate menu items
        List<MenuItem> menuItems = menuItemRepository.findAllById(
                request.items().stream().map(OrderItemRequest::menuItemId).toList()
        );

        Map<Long, MenuItem> itemMap = new HashMap<>();
        for (MenuItem item : menuItems) {
            itemMap.put(item.getId(), item);
        }

        for (OrderItemRequest itemReq : request.items()) {
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

        // 4. Reserve inventory with pessimistic locking
        List<MenuItem> lockedItems = menuItemRepository.findAllById(
                request.items().stream().map(OrderItemRequest::menuItemId).toList()
        );

        Map<Long, MenuItem> lockedMap = new HashMap<>();
        for (MenuItem item : lockedItems) {
            lockedMap.put(item.getId(), item);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.items()) {
            MenuItem item = lockedMap.get(itemReq.menuItemId());
            item.setStockQuantity(item.getStockQuantity() - itemReq.quantity());
            menuItemRepository.save(item);

            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(lineTotal);
        }

        // 5. Create order
        Order order = new Order();
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod("CASH");
        order.setTotalPrice(total);
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        Order savedOrder = orderRepository.save(order);

        // 6. Create order items using menu item name snapshot
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemReq : request.items()) {
            MenuItem item = lockedMap.get(itemReq.menuItemId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(item);
            orderItem.setMenuItemName(item.getName());
            orderItem.setQuantity(itemReq.quantity());
            orderItem.setPrice(item.getPrice());
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);

        // 7. Process payment
        PaymentStatus paymentStatus = paymentService.processPayment(savedOrder.getId());
        if (paymentStatus == PaymentStatus.SUCCESS) {
            savedOrder.setPaymentStatus(PaymentStatus.SUCCESS);
            savedOrder.setPaymentDoneAt(java.time.LocalDateTime.now());
            orderRepository.save(savedOrder);
        } else {
            paymentService.markPaymentFailed(savedOrder.getId());
            savedOrder.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(savedOrder);
            throw new RuntimeException("Payment failed");
        }

        // 8. Publish event after commit
        eventPublisher.publishEvent(new OrderPlacedEvent(savedOrder));

        return toResponse(orderRepository.findById(savedOrder.getId()).orElseThrow());
    }

    @Override
    public OrderResponse acceptOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalOrderStateTransitionException(id, order.getOrderStatus(), OrderStatus.ACCEPTED);
        }

        order.setOrderStatus(OrderStatus.ACCEPTED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse rejectOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalOrderStateTransitionException(id, order.getOrderStatus(), OrderStatus.REJECTED);
        }

        order.setOrderStatus(OrderStatus.REJECTED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse preparingOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalOrderStateTransitionException(id, order.getOrderStatus(), OrderStatus.PREPARING);
        }

        order.setOrderStatus(OrderStatus.PREPARING);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse readyOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.PREPARING) {
            throw new IllegalOrderStateTransitionException(id, order.getOrderStatus(), OrderStatus.READY);
        }

        order.setOrderStatus(OrderStatus.READY);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse pickupOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.READY) {
            throw new IllegalOrderStateTransitionException(id, order.getOrderStatus(), OrderStatus.OUT_OF_DELIVERY);
        }

        order.setOrderStatus(OrderStatus.OUT_OF_DELIVERY);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse deliverOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.OUT_OF_DELIVERY) {
            throw new IllegalOrderStateTransitionException(id, order.getOrderStatus(), OrderStatus.DELIVERED);
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse cancelOrder(Long id) {
        throw new UnsupportedOperationException("Implement customer cancellation");
    }

    @Override
    public List<OrderResponse> getCustomerOrders(Long customerId) {
        throw new UnsupportedOperationException("Implement customer order history");
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        throw new UnsupportedOperationException("Implement with authenticated principal");
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItem> items = order.getOrderItems();
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : items) {
            itemResponses.add(new OrderItemResponse(
                    item.getId(),
                    item.getMenuItem() != null ? item.getMenuItem().getId() : null,
                    item.getMenuItemName(),
                    item.getQuantity(),
                    item.getPrice()
            ));
        }
        return new OrderResponse(
                order.getId(),
                order.getCustomer() != null ? order.getCustomer().getId() : null,
                order.getRestaurant() != null ? order.getRestaurant().getId() : null,
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getPaymentDoneAt(),
                itemResponses
        );
    }
}
