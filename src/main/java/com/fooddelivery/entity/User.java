package com.fooddelivery.entity;

import com.fooddelivery.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String contact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean deleted = false;

    // Customer -> Order
    @OneToMany(mappedBy = "customer")
    private List&lt;Order&gt; orders = new ArrayList<>();

    // RestaurantOwner -> Restaurant
    @OneToMany(mappedBy = "owner")
    private List&lt;Restaurant&gt; ownedRestaurants = new ArrayList<>();

    // DeliveryPartner -> Assignment
    @OneToMany(mappedBy = "deliveryPartner")
    private List&lt;Assignment&gt; assignments = new ArrayList<>();

    // User -> Notification
    @OneToMany(mappedBy = "recipient")
    private List&lt;Notification&gt; notifications = new ArrayList<>();

    // Reviewer -> Review
    @OneToMany(mappedBy = "reviewer")
    private List&lt;Review&gt; reviews = new ArrayList<>();

    // Reviewee -> Review
    @OneToMany(mappedBy = "reviewee")
    private List&lt;Review&gt; receivedReviews = new ArrayList<>();
}