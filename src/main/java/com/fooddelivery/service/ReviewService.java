package com.fooddelivery.service;

import com.fooddelivery.dto.request.ReviewRequest;
import com.fooddelivery.dto.response.ReviewResponse;
import java.util.List;

/**
 * Review and rating queries.
 */
public interface ReviewService {

    /**
     * Find reviews for a reviewee.
     */
    List<ReviewResponse> getReviews(Long revieweeId);

    /**
     * Create a review.
     */
    ReviewResponse createReview(ReviewRequest request);
}
