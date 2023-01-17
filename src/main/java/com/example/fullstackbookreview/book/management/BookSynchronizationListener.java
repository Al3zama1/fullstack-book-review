package com.example.fullstackbookreview.book.management;

import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookSynchronizationListener {

    private final BookRepository bookRepository;
    private final FetchBookMetadata fetchBookMetadata;

    @SqsListener(value = "${sqs.book-synchronization-queue")
    public void consumeBookUpdates(BookSynchronization bookSynchronization) {
        String isbn = bookSynchronization.getIsbn();
        log.info("Incoming book update for isbn '{}'", isbn);

        if (isbn.length() != 13) {
            log.warn("Incoming isbn for book is not 13 characters long, rejecing it");
            return;
        }

        if (bookRepository.findByIsbn(isbn).isPresent()) {
            log.debug("Book with isbn '{}' is already present, rejecting it", isbn);
            return;
        }

        Book book = fetchBookMetadata.fetchMetadataForBook(isbn);
        book = bookRepository.save(book);

        log.info("Successfully stored new book '{}'", book);
    }

}
