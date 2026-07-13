package com.fooddelivery.repository;

import com.fooddelivery.entity.Assignment;
import com.fooddelivery.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByDeliveryPartnerId(Long deliveryPartnerId);
    List<Assignment> findByOrderId(Long orderId);
    Optional<Assignment> findByOrderIdAndAssignmentStatus(Long orderId, AssignmentStatus assignmentStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Assignment> findById(Long id);
}