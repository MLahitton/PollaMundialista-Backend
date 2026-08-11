package com.mundialpolla.teams.api;

import java.util.UUID;

public record TeamResponse(
        UUID id,
        Long externalId,
        String name,
        String shortName,
        String code,
        String countryCode,
        String logoUrl,
        boolean active
) {
}
