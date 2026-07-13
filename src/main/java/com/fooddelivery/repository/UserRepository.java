package com.fooddelivery.repository;

import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByContact(String contact);
    java.util.List<User> findByRoleAndDeletedFalse(Role role);
    boolean existsByContact(String contact);
}