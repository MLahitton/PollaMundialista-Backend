package com.mundialpolla.scoring.api;

import com.mundialpolla.scoring.application.MatchScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/scoring")
@Tag(name = "Internal Scoring")
public class InternalScoringController {

    private final MatchScoringService matchScoringService;

    public InternalScoringController(MatchScoringService matchScoringService) {
        this.matchScoringService = matchScoringService;
    }

    @PostMapping("/matches/{matchId}")
    @Operation(summary = "Score predictions for a match")
    @ApiResponse(responseCode = "200", description = "Match scored")
    @ApiResponse(responseCode = "404", description = "Match not found")
    @ApiResponse(responseCode = "409", description = "Match cannot be scored yet")
    public MatchScoringResultResponse scoreMatch(@PathVariable UUID matchId) {
        return matchScoringService.scoreMatch(matchId);
    }
}
