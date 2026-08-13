package com.mundialpolla.ranking.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TournamentRankingResponse(
        UUID tournamentId,
        Instant asOf,
        List<RankingEntryResponse> top10,
        RankingEntryResponse currentParticipant,
        int rankedParticipants
) {
}
