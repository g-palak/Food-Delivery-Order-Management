package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.request.ReviewRequest;
import com.fooddelivery.dto.response.ReviewResponse;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.ReviewType;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.DomainException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.ReviewRepository;
import com.fooddelivery.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReviewServiceImpl reviewService;

    @Test
    void createReview_success_forRestaurant() {
        ReviewRequest request = new ReviewRequest(1L, 2L, 5, "Great");

        Order order = new Order();
        order.setId(1L);
        User reviewer = new User();
        reviewer.setId(2L);
        reviewer.setRole(Role.CUSTOMER);
        User reviewee = new User();
        reviewee.setId(3L);
        reviewee.setRole(Role.RESTAURANT_OWNER);

        when(reviewRepository.existsByOrderIdAndReviewerId(1L, 2L)).thenReturn(false);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(3L)).thenReturn(Optional.of(reviewee));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(ReviewType.RESTAURANT, response.reviewType());
    }

    @Test
    void createReview_failsWhenDuplicateExists() {
        ReviewRequest request = new ReviewRequest(1L, 2L, 5, "Great");

        when(reviewRepository.existsByOrderIdAndReviewerId(1L, 2L)).thenReturn(true);

        assertThrows(DomainException.class, () -> reviewService.createReview(request));
    }
}
