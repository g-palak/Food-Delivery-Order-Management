package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.response.AssignmentResponse;
import com.fooddelivery.entity.Assignment;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.AssignmentStatus;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.IllegalOrderStateTransitionException;
import com.fooddelivery.repository.AssignmentRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    AssignmentRepository assignmentRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    SecurityContext securityContext;
    @Mock
    org.springframework.security.core.Authentication authentication;

    @InjectMocks
    AssignmentServiceImpl assignmentService;

    private void mockPrincipal(Long userId) {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(String.valueOf(userId), "", List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void acceptAssignment_success() {
        mockPrincipal(5L);

        User partner = new User();
        partner.setId(5L);
        partner.setRole(Role.DELIVERY_PARTNER);

        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatus.READY);

        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setAssignmentStatus(AssignmentStatus.PENDING);
        assignment.setDeliveryPartner(partner);
        assignment.setOrder(order);

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(orderRepository.save(order)).thenReturn(order);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        AssignmentResponse response = assignmentService.acceptAssignment(10L);

        assertNotNull(response);
        assertEquals(AssignmentStatus.ACCEPTED, assignment.getAssignmentStatus());
        assertEquals(OrderStatus.OUT_OF_DELIVERY, order.getOrderStatus());
    }

    @Test
    void acceptAssignment_failsWhenNotPending() {
        mockPrincipal(5L);

        User partner = new User();
        partner.setId(5L);

        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setAssignmentStatus(AssignmentStatus.ACCEPTED);
        assignment.setDeliveryPartner(partner);

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalOrderStateTransitionException.class, () -> assignmentService.acceptAssignment(10L));
    }

    @Test
    void rejectAssignment_success_triggersReassignment() {
        mockPrincipal(5L);

        User partner = new User();
        partner.setId(5L);

        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatus.READY);

        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setAssignmentStatus(AssignmentStatus.PENDING);
        assignment.setDeliveryPartner(partner);
        assignment.setOrder(order);

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByRoleAndDeletedFalse(Role.DELIVERY_PARTNER)).thenReturn(List.of());
        when(orderRepository.save(order)).thenReturn(order);

        AssignmentResponse response = assignmentService.rejectAssignment(10L);

        assertNotNull(response);
        assertEquals(AssignmentStatus.REJECTED, assignment.getAssignmentStatus());
        verify(assignmentRepository).save(assignment);
    }

    @Test
    void pickupOrder_success_transitionsOrderToOutForDelivery() {
        mockPrincipal(5L);

        User partner = new User();
        partner.setId(5L);

        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatus.READY);

        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setAssignmentStatus(AssignmentStatus.ACCEPTED);
        assignment.setDeliveryPartner(partner);
        assignment.setOrder(order);

        when(assignmentRepository.findByOrderIdAndAssignmentStatus(1L, AssignmentStatus.ACCEPTED))
                .thenReturn(Optional.of(assignment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        AssignmentResponse response = assignmentService.pickupOrder(1L);

        assertNotNull(response);
        assertEquals(OrderStatus.OUT_OF_DELIVERY, order.getOrderStatus());
    }

    @Test
    void deliverOrder_success_transitionsOrderToDelivered() {
        mockPrincipal(5L);

        User partner = new User();
        partner.setId(5L);

        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatus.OUT_OF_DELIVERY);

        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setAssignmentStatus(AssignmentStatus.ACCEPTED);
        assignment.setDeliveryPartner(partner);
        assignment.setOrder(order);

        when(assignmentRepository.findByOrderIdAndAssignmentStatus(1L, AssignmentStatus.ACCEPTED))
                .thenReturn(Optional.of(assignment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        AssignmentResponse response = assignmentService.deliverOrder(1L);

        assertNotNull(response);
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
    }

    @Test
    void deliverOrder_failsWhenOrderNotOutForDelivery() {
        mockPrincipal(5L);

        User partner = new User();
        partner.setId(5L);

        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatus.READY);

        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setAssignmentStatus(AssignmentStatus.ACCEPTED);
        assignment.setDeliveryPartner(partner);
        assignment.setOrder(order);

        when(assignmentRepository.findByOrderIdAndAssignmentStatus(1L, AssignmentStatus.ACCEPTED))
                .thenReturn(Optional.of(assignment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOrderStateTransitionException.class, () -> assignmentService.deliverOrder(1L));
    }
}
