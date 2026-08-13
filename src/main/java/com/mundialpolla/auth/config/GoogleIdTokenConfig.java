package com.mundialpolla.auth.config;

import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class GoogleIdTokenConfig {

    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> GOOGLE_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    @Bean(name = "googleIdTokenDecoder")
    @Qualifier("googleIdTokenDecoder")
    JwtDecoder googleIdTokenDecoder(AuthProperties authProperties) {
        String clientId = authProperties.google().clientId();
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID must be configured");
        }
        clientId = clientId.trim();

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build();
        jwtDecoder.setJwtValidator(googleJwtValidator(clientId));
        return jwtDecoder;
    }

    private OAuth2TokenValidator<Jwt> googleJwtValidator(String clientId) {
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> {
            if (GOOGLE_ISSUERS.contains(jwt.getIssuer() == null ? null : jwt.getIssuer().toString())) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Invalid Google ID token issuer",
                    null
            ));
        };
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            if (jwt.getAudience().contains(clientId)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Invalid Google ID token audience",
                    null
            ));
        };

        return token -> {
            OAuth2TokenValidatorResult defaultResult = defaultValidator.validate(token);
            if (defaultResult.hasErrors()) {
                return defaultResult;
            }
            OAuth2TokenValidatorResult issuerResult = issuerValidator.validate(token);
            if (issuerResult.hasErrors()) {
                return issuerResult;
            }
            return audienceValidator.validate(token);
        };
    }
}
