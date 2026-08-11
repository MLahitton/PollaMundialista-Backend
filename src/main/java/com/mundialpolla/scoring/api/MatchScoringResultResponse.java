package com.mundialpolla.scoring.api;

import java.time.Instant;
import java.util.UUID;

public record MatchScoringResultResponse(
        UUID matchId,
        int predictionsProcessed,
        int scoresCreated,
        int scoresUpdated,
        Instant scoredAt
) {
}
