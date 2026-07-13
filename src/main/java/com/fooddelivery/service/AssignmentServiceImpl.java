package com.fooddelivery.service;

import com.fooddelivery.dto.response.AssignmentResponse;
import com.fooddelivery.entity.Assignment;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.AssignmentStatus;
import com.fooddelivery.exception.IllegalOrderStateTransitionException;
import com.fooddelivery.repository.AssignmentRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Delivery assignment workflow.
 *
 * <p>Responsible for matching an order to an available partner,
 * resolving concurrent acceptance safely, and changing assignment state.</p>
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 OrderRepository orderRepository,
                                 UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<AssignmentResponse> getMyAssignments() {
        Long partnerId = currentUserId();
        List<Assignment> assignments = assignmentRepository.findByDeliveryPartnerId(partnerId);
        return assignments.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AssignmentResponse acceptAssignment(Long id) {
        // Acquire exclusive row lock to prevent two partners from accepting simultaneously.
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getAssignmentStatus() != AssignmentStatus.PENDING) {
            throw new IllegalOrderStateTransitionException(
                    assignment.getOrder().getId(),
                    assignment.getAssignmentStatus(),
                    AssignmentStatus.ACCEPTED
            );
        }

        assignment.setAssignmentStatus(AssignmentStatus.ACCEPTED);
        assignment.setUpdatedAt(java.time.LocalDateTime.now());
        Assignment saved = assignmentRepository.save(assignment);

        Order order = saved.getOrder();
        if (order == null || order.getOrderStatus() != com.fooddelivery.enums.OrderStatus.READY) {
            throw new IllegalOrderStateTransitionException(
                    saved.getOrder() != null ? saved.getOrder().getId() : null,
                    order != null ? order.getOrderStatus() : null,
                    com.fooddelivery.enums.OrderStatus.OUT_OF_DELIVERY
            );
        }

        order.setOrderStatus(com.fooddelivery.enums.OrderStatus.OUT_OF_DELIVERY);
        orderRepository.save(order);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AssignmentResponse rejectAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getAssignmentStatus() != AssignmentStatus.PENDING) {
            throw new IllegalOrderStateTransitionException(
                    assignment.getOrder().getId(),
                    assignment.getAssignmentStatus(),
                    AssignmentStatus.REJECTED
            );
        }

        assignment.setAssignmentStatus(AssignmentStatus.REJECTED);
        assignment.setUpdatedAt(java.time.LocalDateTime.now());
        Assignment saved = assignmentRepository.save(assignment);
        assignDeliveryPartnerIfAvailable(saved.getOrder().getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void assignDeliveryPartnerIfAvailable(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != com.fooddelivery.enums.OrderStatus.READY) {
            return;
        }

        List<User> partners = userRepository.findByRoleAndDeletedFalse(com.fooddelivery.enums.Role.DELIVERY_PARTNER);
        if (partners.isEmpty()) {
            return;
        }

        User partner = partners.get(0);

        // Reuse an existing pending assignment if present; otherwise create one.
        Optional<Assignment> existing = assignmentRepository.findByOrderIdAndAssignmentStatus(orderId, AssignmentStatus.PENDING);
        if (existing.isPresent()) {
            return;
        }

        Assignment assignment = new Assignment();
        assignment.setOrder(order);
        assignment.setDeliveryPartner(partner);
        assignment.setAssignmentStatus(AssignmentStatus.PENDING);
        assignment.setUpdatedAt(java.time.LocalDateTime.now());
        assignmentRepository.save(assignment);
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

    private AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getOrder() != null ? assignment.getOrder().getId() : null,
                assignment.getDeliveryPartner() != null ? assignment.getDeliveryPartner().getId() : null,
                assignment.getAssignmentStatus(),
                assignment.getUpdatedAt()
        );
    }
}
