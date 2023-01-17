package com.example.fullstackbookreview.book.review;

import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Builder
public record ReviewRequest(
        @NotEmpty String reviewTitle,
        @NotEmpty String reviewContent,
        @NotNull
        @PositiveOrZero Integer rating
) { }
