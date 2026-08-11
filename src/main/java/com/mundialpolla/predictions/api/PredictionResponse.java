package com.mundialpolla.predictions.api;

import java.time.Instant;
import java.util.UUID;

public record PredictionResponse(
        UUID id,
        UUID participantId,
        UUID matchId,
        UUID tournamentId,
        UUID stageId,
        UUID groupId,
        UUID homeTeamId,
        String homeTeamName,
        UUID awayTeamId,
        String awayTeamName,
        Instant startsAt,
        Instant predictionClosesAt,
        int predictedHomeScore,
        int predictedAwayScore,
        UUID predictedQualifiedTeamId,
        Instant submittedAt,
        Instant updatedAt,
        Instant lockedAt,
        boolean editable,
        boolean locked
) {
}
