package com.mundialpolla.matches.api;

import com.mundialpolla.matches.domain.MatchStatus;
import java.time.Instant;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        UUID tournamentId,
        UUID stageId,
        UUID groupId,
        Long externalId,
        UUID homeTeamId,
        String homeTeamName,
        String homeTeamCode,
        String homeTeamLogoUrl,
        UUID awayTeamId,
        String awayTeamName,
        String awayTeamCode,
        String awayTeamLogoUrl,
        Instant startsAt,
        Instant predictionClosesAt,
        MatchStatus status,
        Integer homeScore,
        Integer awayScore,
        Integer homePenaltyScore,
        Integer awayPenaltyScore,
        UUID qualifiedTeamId,
        Instant resultConfirmedAt,
        Instant scoredAt
) {
}
