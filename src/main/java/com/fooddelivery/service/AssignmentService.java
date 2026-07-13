package com.fooddelivery.service;

import com.fooddelivery.dto.response.AssignmentResponse;
import java.util.List;

public interface AssignmentService {
    List<AssignmentResponse> getMyAssignments();
    AssignmentResponse acceptAssignment(Long id);
    AssignmentResponse rejectAssignment(Long id);
    void assignDeliveryPartnerIfAvailable(Long orderId);
}