package com.mundialpolla.predictions.api;

import jakarta.validation.constraints.Min;
import java.util.UUID;

public record UpsertPredictionRequest(
        @Min(0)
        int predictedHomeScore,
        @Min(0)
        int predictedAwayScore,
        UUID predictedQualifiedTeamId
) {
}
