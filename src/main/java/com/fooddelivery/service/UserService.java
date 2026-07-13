package com.fooddelivery.service;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.UserResponse;
import java.util.List;

/**
 * Manages user identity access for admin and customer-facing operations.
 *
 * <p>Currently used by DeliveryPartner and Customer controllers.
 * Services should enforce role constraints at write time
 * so controllers only read/write data they are allowed to touch.</p>
 */
public interface UserService {

    /**
     * Retrieve a single user by ID.
     *
     * <p>Controllers typically guard visibility with {@code #id == authentication.principal.id} or admin role.</p>
     */
    UserResponse getUser(Long id);

    /**
     * Create a new user.
     *
     * <p>Controllers type: customer self-registration, admin-created delivery partner.
     * Implementations should force the target role from the controller's intent,
     * never trust a client-supplied role.</p>
     */
    UserResponse createUser(UserRequest request);

    /**
     * Update profile fields for a user.
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Soft-delete a user.
     */
    void deleteUser(Long id);

    /**
     * Return the currently authenticated user's profile.
     */
    UserResponse getCurrentUser();

    /**
     * Update the currently authenticated user's profile.
     */
    UserResponse updateCurrentUser(UpdateUserRequest request);

    /**
     * List all delivery partners.
     */
    List<UserResponse> getDeliveryPartners();
}
