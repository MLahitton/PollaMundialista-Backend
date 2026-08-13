package com.mundialpolla.scoring.api;

import java.time.Instant;

public record AutomaticScoringRunResponse(
        Instant asOf,
        int candidates,
        int processed,
        int failed
) {
}
