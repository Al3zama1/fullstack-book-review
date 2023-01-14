package com.example.fullstackbookreview.book.review;

import org.springframework.stereotype.Service;

@Service
public class ReviewVerifierImp implements ReviewVerifier{
    @Override
    public boolean reviewMeetsQualityStandards(String review) {
        return false;
    }
}
