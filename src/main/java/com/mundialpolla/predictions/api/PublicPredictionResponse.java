package com.mundialpolla.predictions.api;

import java.time.Instant;
import java.util.UUID;

public record PublicPredictionResponse(
        UUID predictionId,
        UUID participantId,
        String participantDisplayName,
        String participantProfileImageUrl,
        UUID matchId,
        int predictedHomeScore,
        int predictedAwayScore,
        UUID predictedQualifiedTeamId,
        Instant submittedAt,
        Instant updatedAt
) {
}
