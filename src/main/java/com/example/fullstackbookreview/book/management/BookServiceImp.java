package com.example.fullstackbookreview.book.management;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookServiceImp implements BookService{

    private final BookRepository bookRepository;
    private final QueueMessagingTemplate queueMessagingTemplate;
    private final String bookSynchronizationQueueName;

    public BookServiceImp(BookRepository bookRepository,
                          QueueMessagingTemplate queueMessagingTemplate,
                          @Value("${sqs.book-synchronization-queue}") String bookSynchronizationQueueName) {
        this.bookRepository = bookRepository;
        this.queueMessagingTemplate = queueMessagingTemplate;
        this.bookSynchronizationQueueName = bookSynchronizationQueueName;
    }


    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public void createNewBook(String isbn) {
        Map<String, Object> messageHeaders = Map.of("x-custom-header", UUID.randomUUID().toString());
        queueMessagingTemplate.convertAndSend(bookSynchronizationQueueName, new BookSynchronization(isbn), messageHeaders);
    }
}
