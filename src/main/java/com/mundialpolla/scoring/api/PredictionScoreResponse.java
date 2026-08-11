package com.mundialpolla.scoring.api;

import java.time.Instant;
import java.util.UUID;

public record PredictionScoreResponse(
        UUID id,
        UUID predictionId,
        UUID participantId,
        UUID matchId,
        int predictedHomeScore,
        int predictedAwayScore,
        int actualHomeScore,
        int actualAwayScore,
        UUID predictedQualifiedTeamId,
        UUID actualQualifiedTeamId,
        int basePoints,
        int qualifiedTeamBonus,
        int totalPoints,
        boolean exactScore,
        boolean correctOutcome,
        boolean correctQualifiedTeam,
        Instant scoredAt
) {
}
