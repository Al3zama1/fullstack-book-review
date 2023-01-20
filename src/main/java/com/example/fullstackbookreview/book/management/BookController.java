package com.example.fullstackbookreview.book.management;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public List<Book> getAvailableBooks() {
        return bookService.getAllBooks();
    }

    @PostMapping
    public ResponseEntity<Void> addNewBook(@RequestBody @Valid NewBookRequest newBookRequest) {
        bookService.createNewBook(newBookRequest.isbn());

        return ResponseEntity.status(202).build();
    }
}
