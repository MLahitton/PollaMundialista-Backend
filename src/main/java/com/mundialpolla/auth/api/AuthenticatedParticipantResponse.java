package com.mundialpolla.auth.api;

import java.util.UUID;

public record AuthenticatedParticipantResponse(
        UUID id,
        String email,
        String displayName,
        String profileImageUrl,
        boolean active
) {
}
