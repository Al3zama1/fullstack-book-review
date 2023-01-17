package com.example.fullstackbookreview;


import com.example.fullstackbookreview.book.management.BookRepository;
import com.example.fullstackbookreview.book.review.ReviewRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import initializer.RSAKeyGenerator;
import initializer.WireMockInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import stubs.OAuth2Stubs;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = WireMockInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.1")
            .withDatabaseName("book_review")
            .withUsername("test")
            .withPassword("s3cret");

    static {
        postgreSQLContainer.start();
    }

    protected static final String QUEUE_NAME = UUID.randomUUID().toString();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:p6spy:postgresql://" +
                postgreSQLContainer.getHost() + ":" + postgreSQLContainer.getFirstMappedPort() + "/" +
                postgreSQLContainer.getDatabaseName());
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("sqs.book-synchronization-queue", () -> QUEUE_NAME);
    }

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private RSAKeyGenerator rsaKeyGenerator;
    @Autowired
    private OAuth2Stubs oAuth2Stubs;
    @Autowired
    private WireMockServer wireMockServer;

    @BeforeEach
    void init() {
        this.reviewRepository.deleteAll();
        this.bookRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        this.reviewRepository.deleteAll();
        this.bookRepository.deleteAll();
    }

    protected String getSignedJWT(String username, String email) throws JOSEException {
        return createJWT(username, email);
    }

    protected String getSignedJWT() throws JOSEException {
        return createJWT("duke", "duke@spring.io");
    }

    private String createJWT(String username, String email) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(RSAKeyGenerator.KEY_ID)
                .build();

        JWTClaimsSet payload = new JWTClaimsSet.Builder()
                .issuer(oAuth2Stubs.getIssuerUri())
                .audience("account")
                .subject(username)
                .claim("preferred_username", username)
                .claim("email", email)
                .claim("scope", "openid email profile")
                .claim("azp", "react-client")
                .claim("realm_access", Map.of("roles", List.of()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(120)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(header, payload);
        signedJWT.sign(new RSASSASigner(rsaKeyGenerator.getPrivateKey()));
        return signedJWT.serialize();
    }
}
