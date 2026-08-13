package com.mundialpolla.auth.api;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        AuthenticatedParticipantResponse participant
) {
}
