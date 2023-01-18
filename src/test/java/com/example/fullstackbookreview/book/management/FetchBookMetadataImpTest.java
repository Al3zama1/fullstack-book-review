package com.example.fullstackbookreview.book.management;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/*
When it comes to writing unit tests for API clients, mocking the entire WebClient interaction
with Mockito is usually not a good fit. A better approach is to start a local HTTP server
and mock the HTTP responses from the remote system:
 */

@ExtendWith(SpringExtension.class)
class FetchBookMetadataImpTest {

    private MockWebServer mockWebServer;
    private FetchBookMetadataImp cut;

    @Mock
    private BookConverter mockBookConverter;

    private static final String ISBN = "9780596004651";

    private static String VALID_RESPONSE;

    static  {
        try {
            VALID_RESPONSE = new String(FetchBookMetadataImpTest.class
                    .getClassLoader()
                    .getResourceAsStream("stubs/openlibrary/success-" + ISBN + ".json")
                    .readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
  having a new web server for each test method, we ensure that there is not
  any enqueued responses from a previous test to avoid possible side effects
  that could make our test fail.
   */
    @BeforeEach
    public void setup() throws IOException {

        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1_000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(1))
                        .addHandlerLast(new WriteTimeoutHandler(1)));

        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        this.cut = new FetchBookMetadataImp(
                WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                        .baseUrl(mockWebServer.url("/").toString())
                        .build(),
                mockBookConverter
        );

    }

    @Test
    void shouldReturnBookWhenResultIsSuccess() throws InterruptedException {
        // Given
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(VALID_RESPONSE);

        given(mockBookConverter.convertToBook(ArgumentMatchers.any(), ArgumentMatchers.any())).willReturn(new Book());

        this.mockWebServer.enqueue(mockResponse);

        // When
        Book result = cut.fetchMetadataForBook(ISBN);

        // Then
        assertThat(result).isNotNull();

        // RecordedRequest can be used to verify things like headers, uri, body, etc...
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo(
                "/api/books?jscmd=data&format=json&bibkeys=ISBN:9780596004651");
    }

    @Test
    void shouldPropagateExceptionWhenRemoteSystemIsDown() throws InterruptedException {
        // Given
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .setBody("Sorry, system is down :(");

        this.mockWebServer.enqueue(mockResponse);

        // When
        assertThatThrownBy(() -> cut.fetchMetadataForBook(ISBN)).
                isInstanceOf(RuntimeException.class);

        // Then
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo(
                "/api/books?jscmd=data&format=json&bibkeys=ISBN:9780596004651");
    }

    // make the client more resilient
    @Test
    void shouldRetryWhenRemoteSystemIsSlowOrFailing() throws InterruptedException {
        // Given
        given(mockBookConverter.convertToBook(ArgumentMatchers.any(), ArgumentMatchers.any())).willReturn(new Book());

        this.mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Sorry, system is down :("));

        this.mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(200)
                .setBody(VALID_RESPONSE)
                .setBodyDelay(2, TimeUnit.SECONDS));

        this.mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(200)
                .setBody(VALID_RESPONSE));

        // When
        Book result = cut.fetchMetadataForBook(ISBN);

        // Then
        assertThat(result).isNotNull();
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo(
                "/api/books?jscmd=data&format=json&bibkeys=ISBN:9780596004651");
    }
}