package com.fooddelivery.controller;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.AssignmentResponse;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.service.AssignmentService;
import com.fooddelivery.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final AssignmentService assignmentService;

    public OrderController(OrderService orderService, AssignmentService assignmentService) {
        this.orderService = orderService;
        this.assignmentService = assignmentService;
    }

    // Customer
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(/*customerId from auth*/ null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Restaurant
    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.acceptOrder(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.rejectOrder(id));
    }

    @PatchMapping("/{id}/preparing")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<OrderResponse> preparingOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.preparingOrder(id));
    }

    @PatchMapping("/{id}/ready")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<OrderResponse> readyOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.readyOrder(id));
    }

    // Delivery partner
    @PatchMapping("/{id}/pickup")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<OrderResponse> pickupOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.pickupOrder(id));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.deliverOrder(id));
    }

    // Customer-initiated cancellation
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}