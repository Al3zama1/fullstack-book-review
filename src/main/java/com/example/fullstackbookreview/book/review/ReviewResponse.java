package com.example.fullstackbookreview.book.review;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponse(Long reviewId, String reviewContent, String reviewTitle, Integer rating,
                             Long bookId, String bookIsbn, String bookTitle, String bookThumbnailUrl, String submittedBy,
                             Long submittedAt) { }
