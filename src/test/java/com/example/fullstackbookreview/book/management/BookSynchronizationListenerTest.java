package com.example.fullstackbookreview.book.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BookSynchronizationListenerTest {
    private final static String VALID_ISBN = "1234567891234";

    @Mock
    private BookRepository mockBookRepository;
    @Mock
    private OpenLibraryApiClient mockOpenLibraryApiClient;
    @InjectMocks
    private BookSynchronizationListener cut;
    @Captor
    private ArgumentCaptor<Book> bookArgumentCaptor;

    @Test
    void shouldRejectBookWhenIsbnIsMalformed() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization("42");

        // When
        cut.consumeBookUpdates(bookSynchronization);

        // Then
        then(mockOpenLibraryApiClient).shouldHaveNoInteractions();
        then(mockBookRepository).shouldHaveNoInteractions();
    }

    @Test
    void shouldNotOverrideWhenBookAlreadyExists() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);

        given(mockBookRepository.findByIsbn(bookSynchronization.getIsbn())).willReturn(Optional.of(new Book()));

        // When
        cut.consumeBookUpdates(bookSynchronization);

        // Then
        then(mockOpenLibraryApiClient).shouldHaveNoInteractions();
        then(mockBookRepository).should(never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProcessingFails() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);

        given(mockBookRepository.findByIsbn(bookSynchronization.getIsbn())).willReturn(Optional.empty());
        given(mockOpenLibraryApiClient.fetchMetadataForBook(VALID_ISBN))
                .willThrow(new RuntimeException("Network timeout"));

        // When
        assertThatThrownBy(() -> cut.consumeBookUpdates(bookSynchronization))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Network timeout");

        // Then
        then(mockBookRepository).should(never()).save(any());
    }

    @Test
    void shouldStoreBookWhenNewAndCorrectIsbn() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);
        Book bookToSave = Book.builder()
                        .title("Java Book")
                        .isbn(VALID_ISBN)
                        .build();

        given(mockBookRepository.findByIsbn(bookSynchronization.getIsbn())).willReturn(Optional.empty());
        given(mockOpenLibraryApiClient.fetchMetadataForBook(VALID_ISBN)).willReturn(bookToSave);

        // When
        cut.consumeBookUpdates(bookSynchronization);

        // Then
        then(mockBookRepository).should().save(bookArgumentCaptor.capture());
        Book savedBook = bookArgumentCaptor.getValue();
        assertThat(savedBook).isEqualTo(bookToSave);
    }

}