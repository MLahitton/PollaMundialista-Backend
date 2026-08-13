package com.mundialpolla.me.api;

import com.mundialpolla.auth.application.CurrentParticipantProvider;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.ranking.api.TournamentRankingResponse;
import com.mundialpolla.ranking.application.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/rankings")
@Tag(name = "My Ranking")
@SecurityRequirement(name = "bearerAuth")
public class MeRankingController {

    private final CurrentParticipantProvider currentParticipantProvider;
    private final RankingService rankingService;

    public MeRankingController(CurrentParticipantProvider currentParticipantProvider, RankingService rankingService) {
        this.currentParticipantProvider = currentParticipantProvider;
        this.rankingService = rankingService;
    }

    @GetMapping("/tournaments/{tournamentId}")
    @Operation(summary = "Get my tournament ranking view")
    @ApiResponse(responseCode = "200", description = "Ranking calculated")
    public TournamentRankingResponse getTournamentRanking(@PathVariable UUID tournamentId) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        return rankingService.getTournamentRanking(tournamentId, participant.getId());
    }
}
