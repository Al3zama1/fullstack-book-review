package com.example.fullstackbookreview.book.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest()
@Testcontainers
// don't create the embedded testing database like h2 on its own, instead use specified database
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository cut;

    // spin up postgreSQL container to perform test on a database that is as close as possible to our production database
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.1")
            .withDatabaseName("book_review")
            .withUsername("test")
            .withPassword("test");


    // dynamically set properties based on settings from postgreSQLContainer
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:p6spy:postgresql://" +
                postgreSQLContainer.getHost() + ":" + postgreSQLContainer.getFirstMappedPort() + "/" +
                postgreSQLContainer.getDatabaseName());
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Test
    @Sql("/scripts/INIT_REVIEW_EACH_BOOK.sql")
    void shouldGetTwoReviewStatisticsWhenDatabaseContainsTwoBooksWithReviews() {
        // When -> data provided through sql script file

        // When
        List<ReviewStatistic> reviewStatistics = cut.getReviewStatistics();

        // Then
        assertThat(cut.count()).isEqualTo(3);
        assertThat(reviewStatistics.size()).isEqualTo(2);
        assertThat(reviewStatistics.get(0).getRatings()).isEqualTo(2);
        assertThat(reviewStatistics.get(0).getId()).isEqualTo(2);
    }

}