package com.mundialpolla.participants.api;

import java.util.UUID;

public record ParticipantDevResponse(
        UUID id,
        String email,
        String displayName,
        boolean active
) {
}
