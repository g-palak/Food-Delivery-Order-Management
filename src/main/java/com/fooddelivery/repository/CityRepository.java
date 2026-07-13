package com.fooddelivery.repository;

import com.fooddelivery.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
}