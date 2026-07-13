package com.fooddelivery.service;

import com.fooddelivery.dto.response.AssignmentResponse;
import java.util.List;

/**
 * Delivery assignment workflow.
 *
 * <p>Responsible for matching an order to an available partner,
 * resolving concurrent acceptance safely,
 * and changing assignment state.</p>
 */
public interface AssignmentService {

    /**
     * List assignments for the current delivery partner.
     */
    List<AssignmentResponse> getMyAssignments();

    /**
     * Accept an assignment.
     *
     * <p>Use pessimistic locking to prevent concurrent acceptance race.</p>
     */
    AssignmentResponse acceptAssignment(Long id);

    /**
     * Reject an assignment.
     */
    AssignmentResponse rejectAssignment(Long id);

    /**
     * Attempt to auto-assign an order to an available delivery partner.
     *
     * <p>This is an internal service hook, not directly exposed through controllers.</p>
     */
    void assignDeliveryPartnerIfAvailable(Long orderId);

    /**
     * Delivery partner confirms pickup of an order.
     *
     * <p>Validates assignment ownership, assignment state, and order state.</p>
     */
    AssignmentResponse pickupOrder(Long orderId);

    /**
     * Delivery partner marks order as delivered.
     *
     * <p>Validates assignment ownership and current order state.</p>
     */
    AssignmentResponse deliverOrder(Long orderId);
}
