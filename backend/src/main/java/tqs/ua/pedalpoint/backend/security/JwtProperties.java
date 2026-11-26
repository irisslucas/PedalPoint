package tqs.ua.pedalpoint.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("pedalpoint.jwt")
public record JwtProperties(String secret, String issuer, Duration expiration) {

    /** HS256 needs a key of at least 256 bits. */
    static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "pedalpoint.jwt.secret (PEDALPOINT_JWT_SECRET) must be at least " + MIN_SECRET_BYTES + " bytes");
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "pedalpoint";
        }
        if (expiration == null) {
            expiration = Duration.ofHours(1);
        }
    }
}
