package com.mundialpolla.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AppJwtConfig {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_BYTES = 32;

    @Bean
    SecretKey appJwtSecretKey(AuthProperties authProperties) {
        String secret = authProperties.jwt().secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }

        byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be a valid Base64 secret with at least 32 bytes", exception);
        }
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET must be a valid Base64 secret with at least 32 bytes");
        }

        return new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
    }

    @Bean
    JwtEncoder appJwtEncoder(SecretKey appJwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(appJwtSecretKey));
    }

    @Bean(name = "appJwtDecoder")
    @Primary
    JwtDecoder appJwtDecoder(SecretKey appJwtSecretKey, AuthProperties authProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(appJwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(appJwtValidator(authProperties));
        return jwtDecoder;
    }

    private OAuth2TokenValidator<Jwt> appJwtValidator(AuthProperties authProperties) {
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefaultWithIssuer(authProperties.jwt().issuer());
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            if (jwt.getAudience().contains(authProperties.jwt().audience())) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Invalid JWT audience",
                    null
            ));
        };

        return token -> {
            OAuth2TokenValidatorResult defaultResult = defaultValidator.validate(token);
            if (defaultResult.hasErrors()) {
                return defaultResult;
            }
            return audienceValidator.validate(token);
        };
    }
}
