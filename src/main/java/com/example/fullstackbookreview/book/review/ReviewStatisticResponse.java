package com.example.fullstackbookreview.book.review;

import java.math.BigDecimal;

public record ReviewStatisticResponse(Long bookId, String isbn, BigDecimal avg, Long ratings) { }
