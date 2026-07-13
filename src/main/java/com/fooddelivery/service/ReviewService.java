package com.fooddelivery.service;

import com.fooddelivery.dto.request.ReviewRequest;
import com.fooddelivery.dto.response.ReviewResponse;
import java.util.List;

public interface ReviewService {
    List<ReviewResponse> getReviews(Long revieweeId);
    ReviewResponse createReview(ReviewRequest request);
}