package com.fooddelivery.repository;

import com.fooddelivery.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeId(Long revieweeId);
    Optional<Review> findByOrderIdAndReviewerId(Long orderId, Long reviewerId);
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);
}