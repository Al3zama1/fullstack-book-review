package com.example.fullstackbookreview.book.review;

import com.example.fullstackbookreview.AbstractIntegrationTest;
import com.example.fullstackbookreview.book.management.Book;
import com.example.fullstackbookreview.book.management.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewControllerIT extends AbstractIntegrationTest{

    private static final String ISBN = "9780596004651";


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    ObjectMapper objectMapper;

    /*
    it would be too much to test again the whole process of importing a book because this process has
    already been verified in another integration test.
    Therefore, here it is enough to just populate a book
     */

    @BeforeEach
    public void setup() {
        Book book = new Book();

        book.setPublisher("Duke Inc");
        book.setIsbn(ISBN);
        book.setPages(42L);
        book.setTitle("Joyful testing with Spring Boot");
        book.setDescription("Writing unit and integration test for Spring Boot Applications");
        book.setAuthor("Rieckpil");
        book.setThumbnailUrl("https://rieckpil.de/wp-content/uploads/2020/08/tsbam_introduction_thumbnail-585x329.png.webp");
        book.setGenre("Software Development");

        this.bookRepository.save(book);
    }

    @AfterEach
    public void cleanUp() {
        this.reviewRepository.deleteAll();
    }

    @Test
    void shouldReturnCreatedReviewWhenBookExistsAndReviewHasGoodQuality() throws Exception {
        ReviewRequest reviewRequest = new ReviewRequest(
                "Great book with lots of tips & tricks",
                "I can really recommend reading this book. It includes up-to-date library versions and " +
                        "real-world examples",
                4);

        String validJWT = getSignedJWT();

        HttpHeaders responseHeaders = this.webTestClient
                .post()
                .uri("/api/v1/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(reviewRequest))
                .exchange()
                .expectStatus().isCreated()
                .returnResult(ResponseEntity.class)
                .getResponseHeaders();

        this.webTestClient
                .get()
                .uri(Objects.requireNonNull(responseHeaders.getLocation()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWT)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reviewTitle").isEqualTo("Great book with lots of tips & tricks")
                .jsonPath("$.rating").isEqualTo(4)
                .jsonPath("$.bookIsbn").isEqualTo(ISBN);
    }

    @Test
    void shouldReturnReviewStatisticWhenMultipleReviewsForBookFromDifferentUsersExist() throws JOSEException {
        this.webTestClient
                .post()
                .uri("/api/v1/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " +
                        getSignedJWT("john", "john@spring.io"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
      {
        "reviewTitle" : "Great book with lots of tips & tricks",
        "reviewContent" : "I can really recommend reading this book. It includes up-to-date library versions and real-world examples",
        "rating" : 5
      }
      """)
                .exchange()
                .expectStatus().isCreated();


        this.webTestClient.post()
                .uri("/api/v1/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT("duke",
                        "duke@spring.io"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
      {
        "reviewTitle" : "This book is okay",
        "reviewContent" : "I can recommend reading this book for everyone who wants to get started with testing",
        "rating" : 3
      }
      """)
                .exchange()
                .expectStatus().isCreated();


        this.webTestClient.post()
                .uri("/api/v1/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " +
                        getSignedJWT("mandy", "mandy@spring.io"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
      {
        "reviewTitle" : "This book is great",
        "reviewContent" : "Good content, great example and most of all up-to-date frameworks and libraries",
        "rating" : 4
      }
      """)
                .exchange()
                .expectStatus().isCreated();


        this.webTestClient
                .get()
                .uri("/api/v1/books/reviews/statistics")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size()").isEqualTo(1)
                .jsonPath("$[0].isbn").isEqualTo(ISBN)
                .jsonPath("$[0].ratings").isEqualTo(3)
                .jsonPath("$[0].avg").isEqualTo(4.00);
    }

}
