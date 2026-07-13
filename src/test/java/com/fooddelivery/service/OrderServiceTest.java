package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OrderItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.MenuItemAvailability;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.IllegalOrderStateTransitionException;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.OrderItemRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.InventoryService;
import com.fooddelivery.service.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    RestaurantRepository restaurantRepository;
    @Mock
    MenuItemRepository menuItemRepository;
    @Mock
    PaymentService paymentService;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    InventoryService inventoryService;

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    void placeOrder_success_createsOrderAndPublishesEvent() {
        User customer = new User();
        customer.setId(1L);
        customer.setRole(Role.CUSTOMER);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(2L);
        restaurant.setStatus(RestaurantStatus.OPEN);
        restaurant.setDeleted(false);

        MenuItem menuItem = new MenuItem();
        menuItem.setId(3L);
        menuItem.setName("Pizza");
        menuItem.setPrice(new BigDecimal("100"));
        menuItem.setStockQuantity(10);
        menuItem.setAvailability(MenuItemAvailability.AVAILABLE);
        menuItem.setDeleted(false);

        PlaceOrderRequest request = new PlaceOrderRequest(2L, List.of(new OrderItemRequest(3L, 2)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findAllById(List.of(3L))).thenReturn(List.of(menuItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(100L);
            return o;
        });
        when(orderItemRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(paymentService.processPayment(100L)).thenReturn(PaymentStatus.SUCCESS);
        when(orderRepository.findById(100L)).thenAnswer(i -> {
            Order o = new Order();
            o.setId(100L);
            o.setOrderStatus(OrderStatus.PLACED);
            o.setPaymentStatus(PaymentStatus.SUCCESS);
            o.setTotalPrice(new BigDecimal("200"));
            o.setCustomer(customer);
            o.setRestaurant(restaurant);
            o.setOrderItems(List.of());
            return Optional.of(o);
        });

        OrderResponse response = orderService.placeOrder(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        verify(paymentService).processPayment(100L);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void placeOrder_failsWhenRestaurantUnavailable() {
        User customer = new User();
        customer.setId(1L);
        customer.setRole(Role.CUSTOMER);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(2L);
        restaurant.setStatus(RestaurantStatus.CLOSED);

        PlaceOrderRequest request = new PlaceOrderRequest(2L, List.of(new OrderItemRequest(3L, 2)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> orderService.placeOrder(1L, request));
        assertTrue(ex.getMessage().contains("not accepting orders"));
    }

    @Test
    void acceptOrder_success_transitionsToAccepted() {
        Order order = new Order();
        order.setId(10L);
        order.setOrderStatus(OrderStatus.PLACED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.acceptOrder(10L);

        assertNotNull(response);
        assertEquals(OrderStatus.ACCEPTED, order.getOrderStatus());
    }

    @Test
    void acceptOrder_failsOnInvalidTransition() {
        Order order = new Order();
        order.setId(10L);
        order.setOrderStatus(OrderStatus.ACCEPTED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOrderStateTransitionException.class, () -> orderService.acceptOrder(10L));
    }

    @Test
    void rejectOrder_success_transitionsToRejected() {
        Order order = new Order();
        order.setId(11L);
        order.setOrderStatus(OrderStatus.PLACED);

        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.rejectOrder(11L);

        assertNotNull(response);
        assertEquals(OrderStatus.REJECTED, order.getOrderStatus());
    }

    @Test
    void preparingOrder_failsWhenNotAccepted() {
        Order order = new Order();
        order.setId(12L);
        order.setOrderStatus(OrderStatus.PLACED);

        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOrderStateTransitionException.class, () -> orderService.preparingOrder(12L));
    }

    @Test
    void readyOrder_success_transitionsToReady() {
        Order order = new Order();
        order.setId(13L);
        order.setOrderStatus(OrderStatus.PREPARING);

        when(orderRepository.findById(13L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.readyOrder(13L);

        assertNotNull(response);
        assertEquals(OrderStatus.READY, order.getOrderStatus());
    }

    @Test
    void deliverOrder_failsWhenNotOutForDelivery() {
        Order order = new Order();
        order.setId(14L);
        order.setOrderStatus(OrderStatus.READY);

        when(orderRepository.findById(14L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOrderStateTransitionException.class, () -> orderService.deliverOrder(14L));
    }
}
