package com.example.fullstackbookreview.book.review.qualitycheck;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ReviewVerifierImp implements ReviewVerifier{
    @Override
    public boolean reviewMeetsQualityStandards(String review) {
        if (review.contains("Lorem ipsum")) {
            return false;
        }

        String[] words = review.split(" ");

        if (Arrays.stream(words).filter(s -> s.equalsIgnoreCase("I")).count() >= 5) {
            return false;
        }

        if (Arrays.stream(words).filter(s -> s.equalsIgnoreCase("good")).count() >= 3) {
            return false;
        }

        if (words.length <= 10) return false;

        return doesNotContainSwearWords(review);
    }

    private boolean doesNotContainSwearWords(String review) {
        return !review.contains("shit");
    }

}
