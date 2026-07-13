package com.fooddelivery.entity;

import com.fooddelivery.enums.MenuItemAvailability;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuItemAvailability availability;

    @Column(nullable = false)
    private boolean deleted = false;

    // MenuItem -> Restaurant
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // MenuItem -> OrderItem
    @OneToMany(mappedBy = "menuItem")
    private List&lt;OrderItem&gt; orderItems = new ArrayList<>();
}