package com.fooddelivery.service;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.CustomerNotFoundException;
import com.fooddelivery.exception.DuplicateResourceException;
import com.fooddelivery.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (user.isDeleted()) {
            throw new CustomerNotFoundException(id);
        }

        return toUserResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByContact(request.contact())) {
            throw new DuplicateResourceException("Contact already exists: " + request.contact());
        }

        User user = new User();
        user.setName(request.name());
        user.setContact(request.contact());
        user.setRole(request.role());
        user.setDeleted(false);

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (user.isDeleted()) {
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
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (user.isDeleted()) {
            throw new CustomerNotFoundException(id);
        }

        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        Long authenticatedUserId = currentUserId();
        return getUser(authenticatedUserId);
    }

    @Override
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        Long authenticatedUserId = currentUserId();
        return updateUser(authenticatedUserId, request);
    }

    @Override
    public List<UserResponse> getDeliveryPartners() {
        return userRepository.findByRoleAndDeletedFalse(Role.DELIVERY_PARTNER)
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return Long.parseLong(userDetails.getUsername());
        }
        if (principal instanceof String username) {
            return Long.parseLong(username);
        }
        throw new RuntimeException("Unauthenticated");
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
}
