package com.example.fullstackbookreview.book.review.qualitycheck;

import com.example.fullstackbookreview.book.review.qualitycheck.ReviewVerifierImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewVerifierImpTest {

    private ReviewVerifierImp cut;

    @BeforeEach
    void setup() {
        cut = new ReviewVerifierImp();
    }

    @Test
    void shouldRejectReviewWhenReviewContentContainsLoremIpsum() {
        // Give
        String badReview = "Lorem ipsum";

        // When
        boolean validReview = cut.reviewMeetsQualityStandards(badReview);

        // Then
        assertThat(validReview).isEqualTo(false);
    }

    @Test
    void shouldRejectReviewWhenItContainsMoreThanFiveI() {
        // Given
        String badReview = "I like it because I think it is good. I recommend it. I want to read it again because I enjoy it";

        // When
        boolean validReview = cut.reviewMeetsQualityStandards(badReview);

        // Then
        assertThat(validReview).isEqualTo(false);
    }

    @Test
    void shouldRejectReviewWhenItContainsMoreThanThreeGood() {
        // Given
        String badReview = "It is good and I would read it again because it is good and I like it. I recommend it, it is good";

        // When
        boolean validReview = cut.reviewMeetsQualityStandards(badReview);

        // Then
        assertThat(validReview).isEqualTo(false);
    }

    @Test
    void shouldRejectReviewWhenItContainsLessThanTenCharacters() {
        // Given
        String badReview = "Overall good book";

        // When
        boolean validReview = cut.reviewMeetsQualityStandards(badReview);

        // Then
        assertThat(validReview).isEqualTo(false);
    }

    @Test
    void shouldRejectReviewWhenItContainsSwearWords() {
        // Given
        String badReview = "I like this book because it has a lot good examples but I feel like it is also kind of shit";

        // When
        boolean validReview = cut.reviewMeetsQualityStandards(badReview);

        // Then
        assertThat(validReview).isEqualTo(false);
    }

    @Test
    void shouldAcceptReviewWhenItIsOfGoodQuality() {
        // Given
        String goodReview = "Overall I like this book because it has a lot of relatable examples to real life";

        // When
        boolean validReview = cut.reviewMeetsQualityStandards(goodReview);

        // Then
        assertThat(validReview).isEqualTo(true);
    }
}