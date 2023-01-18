package com.example.fullstackbookreview.book.management;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import io.awspring.cloud.messaging.listener.SimpleMessageListenerContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import io.awspring.cloud.autoconfigure.messaging.SqsAutoConfiguration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@ExtendWith(SpringExtension.class)
@Import(BookSynchronizationListener.class)
@ImportAutoConfiguration(SqsAutoConfiguration.class)
@Slf4j
@Testcontainers
public class BookSynchronizationListenerSliceTest {

    @Container
    static LocalStackContainer localStackContainer = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:0.13.3"))
            .withServices(LocalStackContainer.Service.SQS)
            .withLogConsumer(new Slf4jLogConsumer(log));

    private static final String QUEUE_NAME = UUID.randomUUID().toString();
    private static final String ISBN = "9780596004651";

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        /*
        AWS CLI is wrapped behind the awslocal command.
        basically, these commands are executed on the localstack container
         */
        localStackContainer.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("sqs.book-synchronization-queue", () -> QUEUE_NAME);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AmazonSQSAsync amazonSQS() {
            return AmazonSQSAsyncClientBuilder.standard()
                    .withCredentials(localStackContainer.getDefaultCredentialsProvider())
                    // by default it would be configured to go to an AWS endpoint on the internet
                    // but we want it to go to localhost
                    .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(SQS))
                    .build();
        }

        @Bean
        public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQS) {
            return new QueueMessagingTemplate(amazonSQS);
        }
    }

    @Autowired
    private BookSynchronizationListener cut;
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private SimpleMessageListenerContainer messageListenerContainer;
    @MockBean
    private BookRepository bookRepository;
    @MockBean
    private FetchBookMetadata fetchBookMetadata;

    @Test
    void shouldConsumeMessageWhenPayloadIsCorrect() {
        // Given
        given(bookRepository.findByIsbn(ISBN)).willReturn(Optional.of(new Book()));

        // When
        queueMessagingTemplate.convertAndSend(QUEUE_NAME, new BookSynchronization(ISBN));

        // Then
        Awaitility.given()
                .await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> then(bookRepository).should().findByIsbn(ISBN));
    }
}
