package com.fooddelivery.service;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.CustomerNotFoundException;
import com.fooddelivery.exception.DuplicateResourceException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implements customer-specific operations.
 *
 * <p>Transaction notes:
 * <ul>
 *   <li>{@code createCustomer} is not annotated because creation is a single
 *       database insert; if it fails, Hibernate still flushes within the
 *       underlying transaction and persistence fails are surfaced as
 *       runtime exceptions.</li>
 *   <li>{@code updateCustomer} and {@code updateCurrentCustomer} are not
 *       annotated for the same reason; these are single-entity mutations.</li>
 *   <li>{@code getCustomerOrders} is non-transactional; it only reads.</li>
 * </ul>
 * </p>
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CustomerServiceImpl(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public UserResponse createCustomer(UserRequest request) {
        if (userRepository.existsByContact(request.contact())) {
            throw new DuplicateResourceException("Contact already exists: " + request.contact());
        }

        User user = new User();
        user.setName(request.name());
        user.setContact(request.contact());
        user.setRole(Role.CUSTOMER);
        user.setDeleted(false);

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Override
    public UserResponse getCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (user.isDeleted()) {
            throw new CustomerNotFoundException(id);
        }

        if (user.getRole() != Role.CUSTOMER) {
            throw new CustomerNotFoundException(id);
        }

        return toUserResponse(user);
    }

    @Override
    public UserResponse getCurrentCustomer(Long authenticatedUserId) {
        return getCustomer(authenticatedUserId);
    }

    @Override
    public UserResponse updateCustomer(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (user.isDeleted()) {
            throw new CustomerNotFoundException(id);
        }

        if (user.getRole() != Role.CUSTOMER) {
            throw new CustomerNotFoundException(id);
        }

        if (request.contact() != null && !request.contact().isBlank()) {
            user.setContact(request.contact());
        }
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        User updated = userRepository.save(user);
        return toUserResponse(updated);
    }

    @Override
    public UserResponse updateCurrentCustomer(Long authenticatedUserId, UpdateUserRequest request) {
        return updateCustomer(authenticatedUserId, request);
    }

    @Override
    public List<OrderResponse> getCustomerOrders(Long id) {
        getCustomer(id);
        return orderRepository.findByCustomerId(id)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getMyOrders(Long authenticatedUserId) {
        return getCustomerOrders(authenticatedUserId);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getContact(),
                user.getRole(),
                user.isDeleted()
        );
    }

    private OrderResponse toOrderResponse(com.fooddelivery.entity.Order order) {
        List<com.fooddelivery.dto.response.OrderItemResponse> items = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems().stream()
                .map(item -> new com.fooddelivery.dto.response.OrderItemResponse(
                        item.getId(),
                        item.getMenuItem() != null ? item.getMenuItem().getId() : null,
                        item.getMenuItemName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

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
                items
        );
    }
}
