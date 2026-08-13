package com.mundialpolla.auth.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GoogleIdentityService {

    private static final Logger log = LoggerFactory.getLogger(GoogleIdentityService.class);

    private final JwtDecoder googleIdTokenDecoder;

    public GoogleIdentityService(@Qualifier("googleIdTokenDecoder") JwtDecoder googleIdTokenDecoder) {
        this.googleIdTokenDecoder = googleIdTokenDecoder;
    }

    public GoogleIdentity verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw invalidGoogleToken();
        }

        Jwt jwt;
        try {
            jwt = googleIdTokenDecoder.decode(idToken);
        } catch (JwtValidationException exception) {
            log.warn("Google ID token validation failed: {}", safeValidationErrors(exception));
            throw invalidGoogleToken();
        } catch (JwtException exception) {
            log.warn("Google ID token decoding failed: {}", exception.getClass().getSimpleName());
            throw invalidGoogleToken();
        }

        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        if (isBlank(subject) || isBlank(email) || !Boolean.TRUE.equals(emailVerified)) {
            log.warn("Google ID token claims rejected: {}", rejectedClaimsReason(subject, email, emailVerified));
            throw invalidGoogleToken();
        }

        return new GoogleIdentity(
                subject,
                email,
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("picture")
        );
    }

    private ResponseStatusException invalidGoogleToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safeValidationErrors(JwtValidationException exception) {
        return exception.getErrors().stream()
                .map(error -> error.getErrorCode() + ":" + safeDescription(error.getDescription()))
                .toList()
                .toString();
    }

    private String safeDescription(String description) {
        if (description == null || description.isBlank()) {
            return "no-description";
        }

        return description.length() > 160 ? description.substring(0, 160) : description;
    }

    private String rejectedClaimsReason(String subject, String email, Boolean emailVerified) {
        if (isBlank(subject)) {
            return "missing-sub";
        }
        if (isBlank(email)) {
            return "missing-email";
        }
        if (!Boolean.TRUE.equals(emailVerified)) {
            return "email-not-verified";
        }
        return "invalid-claims";
    }
}
