package com.fooddelivery.entity;

import com.fooddelivery.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus assignmentStatus;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Assignment -> Order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Assignment -> DeliveryPartner
    @ManyToOne
    @JoinColumn(name = "delivery_partner_id", nullable = false)
    private User deliveryPartner;
}