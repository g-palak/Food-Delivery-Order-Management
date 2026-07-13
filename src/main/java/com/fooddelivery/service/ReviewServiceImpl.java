package com.fooddelivery.service;

import com.fooddelivery.dto.request.ReviewRequest;
import com.fooddelivery.dto.response.ReviewResponse;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.ReviewType;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.ReviewRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                              OrderRepository orderRepository,
                              UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ReviewResponse> getReviews(Long revieweeId) {
        return reviewRepository.findByRevieweeId(revieweeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        if (reviewRepository.existsByOrderIdAndReviewerId(request.orderId(), request.reviewerId())) {
            throw new DomainException("Review already exists for order: " + request.orderId());
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new DomainException("Order not found: " + request.orderId()));

        User reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new DomainException("Reviewer not found: " + request.reviewerId()));

        User reviewee = userRepository.findById(request.revieweeId())
                .orElseThrow(() -> new DomainException("Reviewee not found: " + request.revieweeId()));

        ReviewType reviewType;
        if (reviewee.getRole() == com.fooddelivery.enums.Role.RESTAURANT_OWNER) {
            reviewType = ReviewType.RESTAURANT;
        } else if (reviewee.getRole() == com.fooddelivery.enums.Role.DELIVERY_PARTNER) {
            reviewType = ReviewType.DELIVERY_PARTNER;
        } else {
            reviewType = ReviewType.CUSTOMER;
        }

        Review review = new Review();
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setCreatedAt(java.time.LocalDateTime.now());
        review.setReviewType(reviewType);
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);

        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrder() != null ? review.getOrder().getId() : null,
                review.getReviewer() != null ? review.getReviewer().getId() : null,
                review.getReviewee() != null ? review.getReviewee().getId() : null,
                review.getRating(),
                review.getComment(),
                review.getReviewType(),
                review.getCreatedAt()
        );
    }
}
