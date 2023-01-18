package com.example.fullstackbookreview.book.management;

import com.example.fullstackbookreview.AbstractIntegrationTest;
import com.nimbusds.jose.JOSEException;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.testcontainers.shaded.org.awaitility.Awaitility.given;

public class BookSynchronizationListenerIT extends AbstractIntegrationTest {
    private static final String ISBN = "9780596004651";
    private static String VALID_RESPONSE;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private BookRepository bookRepository;

    static {
        try {
            VALID_RESPONSE = new String(BookSynchronizationListenerIT.class
                    .getClassLoader()
                    .getResourceAsStream("stubs/openlibrary/success-" + ISBN + ".json")
                    .readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldGetSuccessWhenClientIsAuthenticated() throws JOSEException {
        this.webTestClient
                .get()
                .uri("/api/v1/books/reviews/statistics")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT())
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
     void shouldReturnBookFromAPIWhenApplicationConsumesNewSyncRequest() {
        this.webTestClient
                .get()
                .uri("/api/v1/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.size()").isEqualTo(0);

        this.openLibraryStubs.stubForSuccessfulBookResponse(ISBN, VALID_RESPONSE);

        this.queueMessagingTemplate.send(QUEUE_NAME, new GenericMessage<>(
                """
                  {
                    "isbn": "%s"
                  }
                  """.formatted(ISBN), Map.of("contentType", "application/json"))
        );

        given()
                .atMost(Duration.ofSeconds(5))
                .await()
                .untilAsserted(() -> {
                    /*
                    here we could also inject the book repository and verify therethat a book has been created.
                    However , with this integration test we want more black boxtest
                    */
                    this.webTestClient
                            .get()
                            .uri("/api/v1/books")
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody()
                            .jsonPath("$.size()").isEqualTo(1)
                            .jsonPath("$[0].isbn").isEqualTo(ISBN);
                });


    }
}
