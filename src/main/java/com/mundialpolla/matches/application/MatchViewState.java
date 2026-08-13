package com.mundialpolla.matches.application;

public record MatchViewState(
        MatchViewStatus status,
        boolean predictionsOpen,
        boolean predictionsClosed,
        boolean resultVisible
) {
}
