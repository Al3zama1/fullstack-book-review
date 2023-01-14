package com.example.fullstackbookreview.book.review;

import com.example.fullstackbookreview.book.management.Book;
import com.example.fullstackbookreview.book.management.BookRepository;
import com.example.fullstackbookreview.exception.BadReviewQualityException;
import com.example.fullstackbookreview.exception.ReviewNotFoundException;
import com.example.fullstackbookreview.user.User;
import com.example.fullstackbookreview.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class ReviewServiceImp implements ReviewService{

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewVerifier reviewVerifier;
    private final UserService userService;

    @Override
    public Long createBookReview(String isbn, ReviewRequest reviewRequest, String username, String email) {
        Optional<Book> bookOptional = bookRepository.findByIsbn(isbn);

        // verify book with given ISBN exists
        if (bookOptional.isEmpty()) throw new IllegalArgumentException();

        // verify BookReviewRequest meets quality standards
        if (!reviewVerifier.reviewMeetsQualityStandards(reviewRequest.reviewContent())) {
            throw new BadReviewQualityException("Review does not meet standards");
        }

        // get user that will be related to new review
        User user = userService.getOrCreateUser(username, email);

        // build and save new review
        Review review = Review.builder()
                .book(bookOptional.get())
                .user(user)
                .content(reviewRequest.reviewContent())
                .title(reviewRequest.reviewTitle())
                .rating(reviewRequest.rating())
                .createdAt(LocalDateTime.now())
                .build();

        review = reviewRepository.save(review);

        return review.getId();
    }

    @Override
    public List<ReviewStatisticResponse> getReviewStatistics() {
        List<ReviewStatisticResponse> reviewStatistics = new ArrayList<>();

        reviewRepository.getReviewStatistics()
                .stream()
                .map(this::mapReviewStatistic)
                .forEach(reviewStatistics::add);

        return reviewStatistics;
    }

    @Override
    public List<ReviewResponse> getAllReviews(Integer size, String orderBy) {
        List<Review> reviews;
        List<ReviewResponse> reviewsResponse = new ArrayList<>();

        if (orderBy.equals("rating")) {
            reviews = reviewRepository.findTop5ByOrderByRatingDescCreatedAtDesc();
        } else {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size));
        }

        reviews.stream()
                .map(this::mapReview)
                .forEach(reviewsResponse::add);

        return reviewsResponse;
    }

    @Override
    public void deleteReview(String isbn, Long reviewId) {
        this.reviewRepository.deleteByIdAndBookIsbn(reviewId, isbn);
    }

    @Override
    public ReviewResponse getReviewById(String isbn, Long reviewId) {
        Optional<Review> reviewOptional = reviewRepository.findByIdAndBookIsbn(reviewId, isbn);

        if (reviewOptional.isEmpty()) throw new ReviewNotFoundException("Review not found");

        return mapReview(reviewOptional.get());
    }

    private ReviewStatisticResponse mapReviewStatistic(ReviewStatistic reviewStatistic) {
        return new ReviewStatisticResponse(reviewStatistic.getId(), reviewStatistic.getIsbn(),
                reviewStatistic.getAvg(), reviewStatistic.getRatings());
    }

    private ReviewResponse mapReview(Review review) {
        return new ReviewResponse(review.getId(), review.getContent(), review.getTitle(), review.getRating(),
                review.getBook().getIsbn(), review.getBook().getTitle(), review.getBook().getThumbnailUrl(),
                review.getUser().getName(), review.getCreatedAt().atZone(ZoneId.of("America/Tijuana"))
                .toInstant().toEpochMilli());
    }
}
