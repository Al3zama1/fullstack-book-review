package com.example.fullstackbookreview.book.review;

import java.time.LocalDateTime;

public record ReviewResponse(Long reviewId, String reviewContent, String reviewTitle, Integer rating,
                             String bookIsbn, String bookTitle, String bookThumbnailUrl, String submittedBy,
                             Long submittedAt) { }
