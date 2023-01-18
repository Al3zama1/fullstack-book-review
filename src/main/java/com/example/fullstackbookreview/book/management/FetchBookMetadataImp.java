package com.example.fullstackbookreview.book.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class FetchBookMetadataImp implements FetchBookMetadata{

    private final WebClient webClient;
    private final BookConverter bookConverter;

    public Book fetchMetadataForBook(String isbn) {

        ObjectNode result = webClient.get().uri("/api/books",
                uriBuilder -> uriBuilder.queryParam("jscmd", "data")
                        .queryParam("format", "json")
                        .queryParam("bibkeys", "ISBN:" + isbn)
                        .build())
                .retrieve()
                .bodyToMono(ObjectNode.class)
                // duration is the time to wait before the next try/invocation
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
                .block();

        JsonNode content = result.get("ISBN:" + isbn);

        return this.bookConverter.convertToBook(isbn, content);
    }
}
