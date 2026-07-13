package com.fooddelivery.service;

import com.fooddelivery.entity.Order;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    public PaymentServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public PaymentStatus processPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Order not found: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            return order.getPaymentStatus();
        }
        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new DomainException("Order is not in a payable state: " + orderId);
        }

        // Simulate successful payment.
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setPaymentDoneAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return PaymentStatus.SUCCESS;
    }

    @Override
    @Transactional
    public PaymentStatus markPaymentSuccess(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Order not found: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new DomainException("Payment is not pending for order: " + orderId);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new DomainException("Order is cancelled: " + orderId);
        }

        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setPaymentDoneAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return PaymentStatus.SUCCESS;
    }

    @Override
    @Transactional
    public PaymentStatus markPaymentFailed(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Order not found: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            return order.getPaymentStatus();
        }

        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);
        return PaymentStatus.FAILED;
    }

    @Override
    @Transactional
    public PaymentStatus refundPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Order not found: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new DomainException("Payment cannot be refunded for order: " + orderId);
        }
        if (order.getOrderStatus() != OrderStatus.DELIVERED && order.getOrderStatus() != OrderStatus.CANCELLED) {
            throw new DomainException("Order is not in a refundable state: " + orderId);
        }

        order.setPaymentStatus(PaymentStatus.REFUNDED);
        orderRepository.save(order);
        return PaymentStatus.REFUNDED;
    }
}
