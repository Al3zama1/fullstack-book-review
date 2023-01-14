package com.example.fullstackbookreview.exception;

public class BadReviewQualityException extends RuntimeException{
    public BadReviewQualityException(String message) {
        super(message);
    }
}
