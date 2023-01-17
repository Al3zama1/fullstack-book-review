package com.example.fullstackbookreview.book.review;

import com.example.fullstackbookreview.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(WebSecurityConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private ReviewService mockReviewService;

    private static final String ISBN = "9780596004651";
    private static final String EMAIL = "john@spring.io";
    private static final String USERNAME = "john";
    private static final long REVIEW_ID = 3;

    @Test
    void shouldReturnTwentyReviewsWithoutAnyOrderWhenNoParametersAreSpecified() throws Exception {
        // Given
        ReviewResponse reviewResponse = ReviewResponse.builder()
                .reviewId(1L)
                .reviewTitle("Good book")
                .build();

        given(mockReviewService.getAllReviews(20, "none")).willReturn(List.of(reviewResponse));

        // When
        mockMvc.perform(get("/api/v1/books/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", Matchers.is(1)))
                .andExpect(jsonPath("$[0].reviewTitle", Matchers.is(reviewResponse.reviewTitle())));

        // Then
    }

    @Test
    void shouldNotReturnReviewStatisticsWhenUserIsUnauthenticated() throws Exception {
        // Given

        // When
        mockMvc.perform(get("/api/v1/books/reviews/statistics"))
                .andExpect(status().isUnauthorized());

        // Then
        then(mockReviewService).shouldHaveNoInteractions();
    }

    @Test
    void shouldReturnReviewStatisticsWhenUserIsAuthenticated() throws Exception {
        // Given
        ReviewStatisticResponse reviewStatisticResponse = new ReviewStatisticResponse(
                1L, ISBN, BigDecimal.valueOf(4), 4L);

        given(mockReviewService.getReviewStatistics()).willReturn(List.of(reviewStatisticResponse));

        // When
        mockMvc.perform(get("/api/v1/books/reviews/statistics")
                        .with(jwt()))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].isbn", Matchers.is(ISBN)));

        // Then
        then(mockReviewService).should().getReviewStatistics();
    }

    @Test
    void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {
        // Given
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .reviewTitle("Great Java Book")
                .reviewContent("I really like this book! It has lots of good examples to learn from.")
                .rating(4)
                .build();

        given(mockReviewService.createBookReview(ISBN, reviewRequest, USERNAME, EMAIL)).willReturn(1L);

        // When
        mockMvc.perform(post("/api/v1/books/{isbn}/reviews", ISBN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest))
                .with(jwt().jwt(builder -> builder
                        .claim("email", EMAIL)
                        .claim("preferred_username", USERNAME))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        Matchers.containsString("/books/%s/reviews/1".formatted(ISBN))));

    }

    @Test
    void shouldRejectNewBookReviewForAuthenticatedUsersWithInvalidPayload() throws Exception {
        // Given
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .reviewContent("I really like this book!")
                .rating(-4).build();

        // When
        this.mockMvc.perform(post("/api/v1/books/{isbn}/reviews", 42)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest))
                        .with(jwt().jwt(builder -> builder.claim("email", EMAIL)
                                .claim("preferred_username", USERNAME))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldNotAllowDeletingReviewsWhenUserIsAuthenticatedWithoutModeratorRole() throws Exception {
        // Given

        // When
        this.mockMvc.perform(delete("/api/v1/books/{isbn}/reviews/{reviewId}", ISBN, REVIEW_ID)
                        .with(jwt()))
                .andExpect(status().isForbidden());

        // Then
        then(mockReviewService).shouldHaveNoInteractions();
    }

    @Test
    @WithMockUser(roles = "moderator")
    void shouldAllowDeletingReviewsWhenUserIsAuthenticatedAndHasModeratorRole() throws Exception {
        // Given

        // When
        this.mockMvc.perform(delete("/api/v1/books/{isbn}/reviews/{reviewId}", ISBN, REVIEW_ID))
                .andExpect(status().isOk());

        // Then
        then(mockReviewService).should().deleteReview(ISBN,  REVIEW_ID);
    }
}