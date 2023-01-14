package com.example.fullstackbookreview.book.review;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ReviewRequest(
        @NotEmpty String reviewTitle,
        @NotEmpty String reviewContent,
        @NotNull
        @PositiveOrZero Integer rating
) { }
