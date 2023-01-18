package initializer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import stubs.OAuth2Stubs;
import stubs.OpenLibraryStubs;

import java.util.Arrays;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        log.info("About to start the WireMockServer");

        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();

        log.info("WireMockServer successfully started");

        if (Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()).contains("integration-test")) {
            RSAKeyGenerator rsaKeyGenerator = new RSAKeyGenerator();
            rsaKeyGenerator.initializeKeys();

            OAuth2Stubs oAuth2Stubs = new OAuth2Stubs(wireMockServer, rsaKeyGenerator);
            /*
            we are initializing oauth2 stubs because these have to be present before the application starts
             */
            oAuth2Stubs.stubForConfiguration();
            oAuth2Stubs.stubForJWKS();

            TestPropertyValues.of(
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:" +
                            wireMockServer.port() + "/auth/realms/spring"
            ).applyTo(applicationContext);

            applicationContext.getBeanFactory().registerSingleton("oAuth2Stubs", oAuth2Stubs);
            applicationContext.getBeanFactory().registerSingleton("rsaKeyGenerator", rsaKeyGenerator);
        }

        /*
        Openlibrary stubs do not need to be initialized since these will only be used used by application during runtime,
        they are not needed before the application loads
         */
        OpenLibraryStubs openLibraryStubs = new OpenLibraryStubs(wireMockServer);

        applicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);
        applicationContext.getBeanFactory().registerSingleton("openLibraryStubs", openLibraryStubs);

        TestPropertyValues.of(
                "clients.open-library.base-url=http://localhost:" + wireMockServer.port() + "/openLibrary"
        ).applyTo(applicationContext);

        applicationContext.addApplicationListener(applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
                log.info("Stopping the WireMockServer");
                wireMockServer.stop();
            }
        });
    }
}
