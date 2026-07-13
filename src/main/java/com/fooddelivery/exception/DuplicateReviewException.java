package com.fooddelivery.exception;

/**
 * Thrown when a review would duplicate an existing review for the same order/reviewer pair.
 */
public class DuplicateReviewException extends DomainException {
    public DuplicateReviewException(String message) {
        super(message);
    }
}
