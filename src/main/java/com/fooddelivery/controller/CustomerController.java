package com.fooddelivery.controller;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final UserService userService;
    private final OrderService orderService;

    public CustomerController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponse> createCustomer(@Valid @RequestBody UserRequest request) {
        // Service must force Role.CUSTOMER
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getCustomerOrders(id));
    }

    // Convenience self endpoints
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentCustomer() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentCustomer(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }

    @GetMapping("/me/orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }
}