package com.fooddelivery.controller;

import com.fooddelivery.dto.response.AssignmentResponse;
import com.fooddelivery.service.AssignmentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assignments")
@PreAuthorize("hasRole('DELIVERY_PARTNER')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getMyAssignments() {
        return ResponseEntity.ok(assignmentService.getMyAssignments());
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<AssignmentResponse> acceptAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.acceptAssignment(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<AssignmentResponse> rejectAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.rejectAssignment(id));
    }
}