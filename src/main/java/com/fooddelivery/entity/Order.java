package com.fooddelivery.entity;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paymentDoneAt;

    // Order -> Customer
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Order -> Restaurant
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Order -> OrderItem
    @OneToMany(mappedBy = "order")
    private List&lt;OrderItem&gt; orderItems = new ArrayList<>();

    // Order -> Assignment
    @OneToMany(mappedBy = "order")
    private List&lt;Assignment&gt; assignments = new ArrayList<>();

    // Order -> Notification
    @OneToMany(mappedBy = "order")
    private List&lt;Notification&gt; notifications = new ArrayList<>();

    // Order -> Review
    @OneToMany(mappedBy = "order")
    private List&lt;Review&gt; reviews = new ArrayList<>();
}