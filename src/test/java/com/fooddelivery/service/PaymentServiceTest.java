package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.entity.Order;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.OrderRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    PaymentServiceImpl paymentService;

    @Test
    void processPayment_success_marksOrderPaid() {
        Order order = new Order();
        order.setId(1L);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PLACED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        PaymentStatus status = paymentService.processPayment(1L);

        assertEquals(PaymentStatus.SUCCESS, status);
        verify(orderRepository).save(order);
    }

    @Test
    void processPayment_failsWhenOrderAlreadyProcessed() {
        Order order = new Order();
        order.setId(1L);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setOrderStatus(OrderStatus.PLACED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        PaymentStatus status = paymentService.processPayment(1L);

        assertEquals(PaymentStatus.SUCCESS, status);
    }

    @Test
    void markPaymentSuccess_failsWhenNotPending() {
        Order order = new Order();
        order.setId(1L);
        order.setPaymentStatus(PaymentStatus.SUCCESS);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(DomainException.class, () -> paymentService.markPaymentSuccess(1L));
    }

    @Test
    void refundPayment_success_changesStatusToRefunded() {
        Order order = new Order();
        order.setId(1L);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setOrderStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        PaymentStatus status = paymentService.refundPayment(1L);

        assertEquals(PaymentStatus.REFUNDED, status);
    }
}
