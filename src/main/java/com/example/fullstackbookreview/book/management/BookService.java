package com.example.fullstackbookreview.book.management;

import java.util.List;

public interface BookService {
    List<Book> getAllBooks();

    void createNewBook(String isbn);
}
