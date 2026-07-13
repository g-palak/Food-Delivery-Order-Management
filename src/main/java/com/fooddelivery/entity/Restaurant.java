package com.fooddelivery.entity;

import com.fooddelivery.enums.RestaurantStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RestaurantStatus status;

    @Column(nullable = false)
    private boolean deleted = false;

    // Restaurant -> City
    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    // Restaurant -> Owner (User with RESTAURANT_OWNER role)
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Restaurant -> MenuItem
    @OneToMany(mappedBy = "restaurant")
    private List&lt;MenuItem&gt; menuItems = new ArrayList<>();

    // Restaurant -> Order
    @OneToMany(mappedBy = "restaurant")
    private List&lt;Order&gt; orders = new ArrayList<>();
}