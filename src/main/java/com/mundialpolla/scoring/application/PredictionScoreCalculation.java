package com.mundialpolla.scoring.application;

public record PredictionScoreCalculation(
        int basePoints,
        int qualifiedTeamBonus,
        int totalPoints,
        boolean exactScore,
        boolean correctOutcome,
        boolean correctQualifiedTeam
) {
}
