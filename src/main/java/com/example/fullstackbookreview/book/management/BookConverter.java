package com.example.fullstackbookreview.book.management;

import com.fasterxml.jackson.databind.JsonNode;

public interface BookConverter {

    Book convertToBook(String isbn, JsonNode content);
}
