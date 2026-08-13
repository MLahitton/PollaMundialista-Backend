package com.mundialpolla.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        Google google,
        Jwt jwt
) {

    public record Google(
            String clientId
    ) {
    }

    public record Jwt(
            String secret,
            String issuer,
            String audience,
            Duration ttl
    ) {
    }
}
