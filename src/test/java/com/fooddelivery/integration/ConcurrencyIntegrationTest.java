package com.fooddelivery.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fooddelivery.entity.Assignment;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.AssignmentStatus;
import com.fooddelivery.enums.MenuItemAvailability;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.enums.Role;
import com.fooddelivery.repository.AssignmentRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.AssignmentService;
import com.fooddelivery.service.InventoryService;
import com.fooddelivery.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ConcurrencyIntegrationTest {

    @Autowired
    OrderService orderService;

    @Autowired
    AssignmentService assignmentService;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    MenuItemRepository menuItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Test
    void twoCustomers_orderLastMenuItem_concurrency() throws Exception {
        User customer1 = new User();
        customer1.setName("Customer 1");
        customer1.setContact("c1");
        customer1.setRole(Role.CUSTOMER);
        customer1.setDeleted(false);
        customer1 = userRepository.save(customer1);

        User customer2 = new User();
        customer2.setName("Customer 2");
        customer2.setContact("c2");
        customer2.setRole(Role.CUSTOMER);
        customer2.setDeleted(false);
        customer2 = userRepository.save(customer2);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Pizza Place");
        restaurant.setStatus(RestaurantStatus.OPEN);
        restaurant.setDeleted(false);
        restaurant = restaurantRepository.save(restaurant);

        MenuItem menuItem = new MenuItem();
        menuItem.setName("Last Pizza");
        menuItem.setPrice(new BigDecimal("100"));
        menuItem.setStockQuantity(1);
        menuItem.setAvailability(MenuItemAvailability.AVAILABLE);
        menuItem.setDeleted(false);
        menuItem.setRestaurant(restaurant);
        menuItem = menuItemRepository.save(menuItem);

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Long customerId = (i == 0) ? customer1.getId() : customer2.getId();
            futures.add(executor.submit(() -> {
                try {
                    barrier.await();
                    com.fooddelivery.dto.request.PlaceOrderRequest request =
                            new com.fooddelivery.dto.request.PlaceOrderRequest(
                                    restaurant.getId(),
                                    List.of(new com.fooddelivery.dto.request.OrderItemRequest(
                                            menuItem.getId(), 1))
                            );
                    orderService.placeOrder(customerId, request);
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
                return null;
            }));
        }

        latch.await();
        executor.shutdown();

        MenuItem finalMenuItem = menuItemRepository.findById(menuItem.getId()).orElseThrow();
        assertTrue(finalMenuItem.getStockQuantity() >= 0, "Stock must never be negative");

        assertEquals(1, orderRepository.count(), "Only one order should be created");
        assertEquals(1, successCount.get(), "Exactly one customer should succeed");
        assertEquals(1, failureCount.get(), "Exactly one customer should fail");
    }

    @Test
    void twoPartners_acceptSameAssignment_concurrency() throws Exception {
        User partner1 = new User();
        partner1.setName("Partner 1");
        partner1.setContact("p1");
        partner1.setRole(Role.DELIVERY_PARTNER);
        partner1.setDeleted(false);
        partner1 = userRepository.save(partner1);

        User partner2 = new User();
        partner2.setName("Partner 2");
        partner2.setContact("p2");
        partner2.setRole(Role.DELIVERY_PARTNER);
        partner2.setDeleted(false);
        partner2 = userRepository.save(partner2);

        User customer = new User();
        customer.setName("Customer");
        customer.setContact("c");
        customer.setRole(Role.CUSTOMER);
        customer.setDeleted(false);
        customer = userRepository.save(customer);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Restaurant");
        restaurant.setStatus(RestaurantStatus.OPEN);
        restaurant.setDeleted(false);
        restaurant = restaurantRepository.save(restaurant);

        Order order = new Order();
        order.setOrderStatus(OrderStatus.PLACED);
        order.setTotalPrice(new BigDecimal("200"));
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order = orderRepository.save(order);

        Assignment assignment = new Assignment();
        assignment.setAssignmentStatus(AssignmentStatus.PENDING);
        assignment.setUpdatedAt(java.time.LocalDateTime.now());
        assignment.setOrder(order);
        assignment.setDeliveryPartner(partner1);
        assignment = assignmentRepository.save(assignment);

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Long partnerId = (i == 0) ? partner1.getId() : partner2.getId();
            Long assignmentId = assignment.getId();
            futures.add(executor.submit(() -> {
                try {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    String.valueOf(partnerId), null, List.of())
                    );
                    barrier.await();
                    assignmentService.acceptAssignment(assignmentId);
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    latch.countDown();
                }
                return null;
            }));
        }

        latch.await();
        executor.shutdown();

        Assignment finalAssignment = assignmentRepository.findById(assignment.getId()).orElseThrow();
        assertEquals(AssignmentStatus.ACCEPTED, finalAssignment.getAssignmentStatus());

        assertEquals(1, successCount.get(), "Exactly one partner should accept the assignment");
        assertEquals(1, failureCount.get(), "Exactly one partner should be rejected");
        assertEquals(partner1.getId(), finalAssignment.getDeliveryPartner().getId());
    }
}
