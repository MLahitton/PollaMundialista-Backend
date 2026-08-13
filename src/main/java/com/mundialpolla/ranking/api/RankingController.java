package com.mundialpolla.ranking.api;

import com.mundialpolla.ranking.application.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@Tag(name = "Rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/tournaments/{tournamentId}")
    @Operation(summary = "Get tournament ranking")
    @ApiResponse(responseCode = "200", description = "Ranking calculated")
    @ApiResponse(responseCode = "404", description = "Tournament or participant not found")
    public TournamentRankingResponse getTournamentRanking(
            @PathVariable UUID tournamentId,
            @RequestParam UUID currentParticipantId
    ) {
        return rankingService.getTournamentRanking(tournamentId, currentParticipantId);
    }
}
