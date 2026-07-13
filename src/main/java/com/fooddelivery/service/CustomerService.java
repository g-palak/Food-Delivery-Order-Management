package com.fooddelivery.service;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.dto.response.OrderResponse;
import java.util.List;

/**
 * Customer-specific operations.
 *
 * <p>A customer is a {@code User} with role {@code Role.CUSTOMER}.
 * Services force that role internally; callers must not pass
 * a role-selection request to customer endpoints.</p>
 *
 * <p>Spring Security is responsible for authorization,
 * so these methods do not enforce role access.</p>
 */
public interface CustomerService {

    /**
     * Create a new customer account.
     *
     * <p>Always forces {@code Role.CUSTOMER}.
     * Throws {@link com.fooddelivery.exception.DuplicateResourceException}
     * if contact already exists.</p>
     */
    UserResponse createCustomer(UserRequest request);

    /**
     * Return customer profile by ID.
     *
     * <p>Throws {@link com.fooddelivery.exception.CustomerNotFoundException}
     * if missing or soft-deleted.</p>
     */
    UserResponse getCustomer(Long id);

    /**
     * Return the currently authenticated customer's profile.
     */
    UserResponse getCurrentCustomer(Long authenticatedUserId);

    /**
     * Update customer profile by ID.
     */
    UserResponse updateCustomer(Long id, UpdateUserRequest request);

    /**
     * Update the currently authenticated customer's profile.
     */
    UserResponse updateCurrentCustomer(Long authenticatedUserId, UpdateUserRequest request);

    /**
     * List all orders for a customer.
     */
    List<OrderResponse> getCustomerOrders(Long id);

    /**
     * List all orders for the currently authenticated customer.
     */
    List<OrderResponse> getMyOrders(Long authenticatedUserId);
}
