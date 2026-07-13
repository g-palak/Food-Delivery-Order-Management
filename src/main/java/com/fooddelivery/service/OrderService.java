package com.fooddelivery.service;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse getOrder(Long id);
    OrderResponse placeOrder(Long customerId, PlaceOrderRequest request);
    OrderResponse acceptOrder(Long id);
    OrderResponse rejectOrder(Long id);
    OrderResponse preparingOrder(Long id);
    OrderResponse readyOrder(Long id);
    OrderResponse pickupOrder(Long id);
    OrderResponse deliverOrder(Long id);
    OrderResponse cancelOrder(Long id);
    List<OrderResponse> getCustomerOrders(Long customerId);
    List<OrderResponse> getMyOrders();
}