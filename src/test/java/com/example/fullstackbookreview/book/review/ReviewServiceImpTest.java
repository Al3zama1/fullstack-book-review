package com.example.fullstackbookreview.book.review;

import com.example.fullstackbookreview.book.management.Book;
import com.example.fullstackbookreview.book.management.BookRepository;
import com.example.fullstackbookreview.book.review.qualitycheck.ReviewVerifier;
import com.example.fullstackbookreview.exception.BadReviewQualityException;
import com.example.fullstackbookreview.exception.ReviewNotFoundException;
import com.example.fullstackbookreview.user.User;
import com.example.fullstackbookreview.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImpTest {

    @Mock
    private ReviewRepository mockReviewRepository;
    @Mock
    private BookRepository mockBookRepository;
    @Mock
    private ReviewVerifier mockReviewVerifier;
    @Mock
    private UserService userService;
    @InjectMocks
    private ReviewServiceImp cut;

    private static final String EMAIL = "john@spring.io";
    private static final String USERNAME = "john";
    private static final String ISBN = "9780596004651";

    @Test
    void shouldThrowExceptionWhenReviewedBookIsNotExisting() {
        // Given
        given(mockBookRepository.findByIsbn(ISBN)).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> cut.createBookReview(ISBN, null, USERNAME, EMAIL))
                .isInstanceOf(IllegalArgumentException.class);

        // Then
        then(mockReviewRepository).shouldHaveNoInteractions();
    }

    @Test
    void shouldRejectReviewWhenReviewQualityIsBad() {
        // Given
        ReviewRequest reviewRequest = new ReviewRequest(
                "Title", "bad content", 1);

        given(mockBookRepository.findByIsbn(ISBN)).willReturn(Optional.of(new Book()));
        given(mockReviewVerifier.reviewMeetsQualityStandards(reviewRequest.reviewContent())).willReturn(false);

        // When
        assertThatThrownBy(() -> cut.createBookReview(ISBN, reviewRequest, USERNAME, EMAIL))
                .isInstanceOf(BadReviewQualityException.class);

        // Then
        then(mockReviewRepository).shouldHaveNoInteractions();
    }

    @Test
    void shouldThrowReviewNotFoundExceptionWhenReviewDoesNotExist() {
        // Given
        Long reviewId = 1L;
        given(mockReviewRepository.findByIdAndBookIsbn(reviewId, ISBN)).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> cut.getReviewById(ISBN, reviewId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void shouldReturnReviewWhenItExists() {
        // Given
        Long reviewId = 1L;
        Book book = new Book(1L, "title", ISBN, "author", "genre", "thumbnail",
                "description", "publisher", 20L);
        User user = new User(1L, USERNAME, EMAIL, LocalDateTime.now());
        Review review = new Review(1L, "review title", "content", 4, LocalDateTime.now(), book, user);

        given(mockReviewRepository.findByIdAndBookIsbn(reviewId, ISBN)).willReturn(Optional.of(review));

        // When
        ReviewResponse reviewResponse = cut.getReviewById(ISBN, reviewId);

        // Then
        assertThat(reviewResponse.bookIsbn()).isEqualTo(ISBN);
        assertThat(reviewResponse.reviewId()).isEqualTo(reviewId);
    }

    @Test
    void shouldStoreReviewWhenReviewQualityIsGoodAndBookIsPresent() {
        // Given
        ReviewRequest reviewRequest = new ReviewRequest(
                "Title", "Good content", 1);

        given(mockBookRepository.findByIsbn(ISBN)).willReturn(Optional.of(new Book()));
        given(mockReviewVerifier.reviewMeetsQualityStandards(reviewRequest.reviewContent())).willReturn(true);
        given(userService.getOrCreateUser(USERNAME, EMAIL)).willReturn(new User());
        given(mockReviewRepository.save(any(Review.class))).willAnswer(invocation -> {
            // the id would be set by DB at runtime, but for testing we have to set it manually
            // good scenario to use willAnswer from mockito
            Review reviewToSave = invocation.getArgument(0);
            reviewToSave.setId(1L);
            return reviewToSave;
        });

        // When
        Long result = cut.createBookReview(ISBN, reviewRequest, USERNAME, EMAIL);

        // Then
        assertThat(result).isEqualTo(1L);

    }

}