package com.mundialpolla.ranking.api;

import java.util.UUID;

public record RankingEntryResponse(
        UUID participantId,
        String displayName,
        String profileImageUrl,
        long position,
        int totalPoints,
        int exactScores,
        int correctOutcomes,
        int qualifiedTeamBonuses,
        int scoredPredictions,
        boolean currentParticipant
) {
}
