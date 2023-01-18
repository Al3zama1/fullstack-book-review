package com.example.fullstackbookreview.book.management;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class BookConverterImp implements BookConverter{
    @Override
    public Book convertToBook(String isbn, JsonNode content) {
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
