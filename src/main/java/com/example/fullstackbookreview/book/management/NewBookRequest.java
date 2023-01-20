package com.example.fullstackbookreview.book.management;

import javax.validation.constraints.Size;

public record NewBookRequest(@Size(min = 13, max = 13) String isbn) { }
