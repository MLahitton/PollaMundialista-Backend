package com.mundialpolla.auth.application;

import java.time.Instant;

public record IssuedJwt(
        String token,
        Instant issuedAt,
        Instant expiresAt
) {
}
