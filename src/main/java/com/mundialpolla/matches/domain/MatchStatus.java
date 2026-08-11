package com.mundialpolla.matches.domain;

public enum MatchStatus {
    SCHEDULED,
    OPEN_FOR_PREDICTIONS,
    PREDICTION_CLOSED,
    IN_PROGRESS,
    FINISHED,
    POSTPONED,
    CANCELLED,
    PENDING_REVIEW,
    SCORED
}
