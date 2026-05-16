package com.uuriturg.landlord.exception;

public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException() {
        super("User has already submitted a review for this landlord");
    }
}
