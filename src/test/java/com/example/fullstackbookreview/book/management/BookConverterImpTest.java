package com.example.fullstackbookreview.book.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class BookConverterImpTest {
    private BookConverterImp bookConverterImp;

    @BeforeEach
    void setup() {
        this.bookConverterImp = new BookConverterImp();
    }

    @Test
    void shouldReturnBookWhenResultIsSuccess() throws IOException {
        // Given
        String isbn = "9780596004651";
        String jsonData = new String(BookConverterImpTest.class
                .getClassLoader()
                .getResourceAsStream("stubs/openlibrary/success-" + isbn + ".json")
                .readAllBytes());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode content = objectMapper.readTree(jsonData).get("ISBN:" + isbn);


        // When
        Book book = this.bookConverterImp.convertToBook(isbn, content);

        // Then
        assertThat(book).isNotNull();
        assertThat(book.getIsbn()).isEqualTo(isbn);
        assertThat(book.getTitle()).isEqualTo("Head first Java");
        assertThat(book.getThumbnailUrl()).isEqualTo("https://covers.openlibrary.org/b/id/388761-S.jpg");
        assertThat(book.getAuthor()).isEqualTo("Kathy Sierra");
        assertThat(book.getDescription()).isEqualTo("Your brain on Java--a learner's guide--Cover.Includes index.");
        assertThat(book.getGenre()).isEqualTo("Java (Computer program language)");
        assertThat(book.getPublisher()).isEqualTo("O'Reilly");
        assertThat(book.getPages()).isEqualTo(619);
        // we don't want an Id yet since it should be created by our DB later
        assertThat(book.getId()).isNull();

    }

    @Test
    void shouldReturnBookWhenResultIsSuccessButLackingAllInformation() throws JsonProcessingException {
        // Given
        String isbn = "9780596004651";
        String jsonData = """
      {
        "ISBN:9780596004651": {
          "publishers": [
            {
              "name": "O'Reilly"
            }
          ],
          "title": "Head second Java",
          "authors": [
            {
            "url": "https://openlibrary.org/authors/OL1400543A/Kathy_Sierra",
            "name": "Kathy Sierra"
            }
          ],
          "number_of_pages": 42,
          "cover": {
            "small": "https://covers.openlibrary.org/b/id/388761-S.jpg",
            "large": "https://covers.openlibrary.org/b/id/388761-L.jpg",
            "medium": "https://covers.openlibrary.org/b/id/388761-M.jpg"
          }
        }
      }
      """;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode content = objectMapper.readTree(jsonData).get("ISBN:" + isbn);

        // When
        Book book = this.bookConverterImp.convertToBook(isbn, content);

        // Then
        assertThat(book).isNotNull();

        assertThat(book.getIsbn()).isEqualTo(isbn);
        assertThat(book.getTitle()).isEqualTo("Head second Java");
        assertThat(book.getThumbnailUrl()).isEqualTo("https://covers.openlibrary.org/b/id/388761-S.jpg");
        assertThat(book.getAuthor()).isEqualTo("Kathy Sierra");
        assertThat(book.getDescription()).isEqualTo("n.A");
        assertThat(book.getGenre()).isEqualTo("n.A");
        assertThat(book.getPublisher()).isEqualTo("O'Reilly");
        assertThat(book.getPages()).isEqualTo(42);
        // we don't want an Id yet since it should be created by our DB later
        assertThat(book.getId()).isNull();
    }

}