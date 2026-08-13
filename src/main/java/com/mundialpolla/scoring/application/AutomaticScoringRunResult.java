package com.mundialpolla.scoring.application;

import java.time.Instant;

public record AutomaticScoringRunResult(
        Instant asOf,
        int candidates,
        int processed,
        int failed
) {
}
