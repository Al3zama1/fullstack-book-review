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

        return convertToBook(isbn, content);
    }

    private Book convertToBook(String isbn, JsonNode content) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setThumbnailUrl(content.get("cover").get("small").asText());
        book.setTitle(content.get("title").asText());
        book.setAuthor(content.get("authors").get(0).get("name").asText());
        book.setPublisher(content.get("publishers").get(0).get("name").asText("n.A."));
        book.setPages(content.get("number_of_pages").asLong(0));
        book.setDescription(content.get("notes") == null ? "n.A" : content.get("notes").asText("n.A."));
        book.setGenre(content.get("subjects") == null ? "n.A" : content.get("subjects").get(0).get("name").asText("n.A."));
        return book;
    }
}
