package com.example.fullstackbookreview.book.review;

import com.example.fullstackbookreview.user.User;
import com.example.fullstackbookreview.book.management.Book;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "review")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private Integer rating;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(
          name = "book_id",
          referencedColumnName = "id"
  )
  private Book book;

  @ManyToOne
  @JoinColumn(
          name = "user_id",
          referencedColumnName = "id"
  )
  private User user;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Review review = (Review) o;
    return Objects.equals(id, review.id) && Objects.equals(title, review.title) &&
            Objects.equals(content, review.content) && Objects.equals(rating, review.rating) &&
            Objects.equals(createdAt, review.createdAt) && Objects.equals(book, review.book) &&
            Objects.equals(user, review.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, content, rating, createdAt, book, user);
  }
}
