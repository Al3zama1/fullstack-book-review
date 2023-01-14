package com.example.fullstackbookreview.book.review;

import java.util.List;

public interface ReviewService {
    Long createBookReview(String isbn, ReviewRequest reviewRequest, String username, String email);
    List<ReviewStatisticResponse> getReviewStatistics();
    List<ReviewResponse> getAllReviews(Integer size, String orderBy);
    void deleteReview(String isbn, Long reviewId);
    ReviewResponse getReviewById(String isbn, Long reviewId);
}
