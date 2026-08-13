package com.mundialpolla.ranking.domain;

import com.mundialpolla.participants.domain.Participant;

public record ParticipantRankingSnapshot(
        Participant participant,
        int totalPoints,
        int exactScores,
        int correctOutcomes,
        int qualifiedTeamBonuses,
        int scoredPredictions
) {

    public ParticipantRankingSnapshot addScore(
            int points,
            boolean exactScore,
            boolean correctOutcome,
            int qualifiedTeamBonus
    ) {
        return new ParticipantRankingSnapshot(
                participant,
                totalPoints + points,
                exactScores + count(exactScore),
                correctOutcomes + count(correctOutcome),
                qualifiedTeamBonuses + qualifiedTeamBonus,
                scoredPredictions + 1
        );
    }

    private static int count(boolean value) {
        return value ? 1 : 0;
    }
}
