package initializer;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.Getter;
import org.checkerframework.checker.units.qual.K;

import java.security.*;
import java.security.interfaces.RSAPublicKey;

@Getter
public class RSAKeyGenerator {
    public static final String KEY_ID = "reviewr";
    private PublicKey publicKey;
    private PrivateKey privateKey;

    /*
    Although there are established representations for all keys, the JWK specification aims at providing a unified
    representation for all keys supported in the JSON Web Algorithms (JWA) specification. A unified representation
    format for keys allows easy sharing and keeps keys independent from the intricacies of other key exchange formats.
     */
    public String getJWKSetJsonString() {
        RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(KEY_ID);

        return new JWKSet(builder.build()).toString();
    }

    public void initializeKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();

            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
