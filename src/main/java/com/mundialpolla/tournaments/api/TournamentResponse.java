package com.mundialpolla.tournaments.api;

import java.time.LocalDate;
import java.util.UUID;

public record TournamentResponse(
        UUID id,
        Long externalId,
        String name,
        String season,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
}
