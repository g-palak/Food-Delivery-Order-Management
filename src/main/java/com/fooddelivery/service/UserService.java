package com.fooddelivery.service;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse getUser(Long id);
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UpdateUserRequest request);
    List<UserResponse> getDeliveryPartners();
    List<UserResponse> getCustomers();
}