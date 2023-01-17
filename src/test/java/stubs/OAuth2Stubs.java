package stubs;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import initializer.RSAKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

@RequiredArgsConstructor
public class OAuth2Stubs {

    private final WireMockServer wireMockServer;
    private final RSAKeyGenerator rsaKeyGenerator;


    public void stubForJWKS() {
        wireMockServer.stubFor(
                WireMock.get("/jwks")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(rsaKeyGenerator.getJWKSetJsonString()))
        );
    }

    public void stubForConfiguration() {
        wireMockServer.stubFor(
                WireMock.get("/auth/realms/spring/.well-known/openid-configuration")
                        .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(
                                    """
                                    {
                                        "issuer":"%s",
                                        "jwks_uri":"%s"
                                    }
                                    """.formatted(getIssuerUri(), getJWKSUri())))
        );
    }


    public String getIssuerUri() {
        return "http://localhost:" + wireMockServer.port() + "/auth/realms/spring";
    }

    public String getJWKSUri() {
        return "http://localhost:" + wireMockServer.port() + "/jwks";
    }

}
