package com.mundialpolla.scoring.api;

import com.mundialpolla.scoring.application.AutomaticScoringRunResult;
import com.mundialpolla.scoring.application.AutomaticScoringService;
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
    private final AutomaticScoringService automaticScoringService;

    public InternalScoringController(
            MatchScoringService matchScoringService,
            AutomaticScoringService automaticScoringService
    ) {
        this.matchScoringService = matchScoringService;
        this.automaticScoringService = automaticScoringService;
    }

    @PostMapping("/matches/{matchId}")
    @Operation(summary = "Score predictions for a match")
    @ApiResponse(responseCode = "200", description = "Match scored")
    @ApiResponse(responseCode = "404", description = "Match not found")
    @ApiResponse(responseCode = "409", description = "Match cannot be scored yet")
    public MatchScoringResultResponse scoreMatch(@PathVariable UUID matchId) {
        return matchScoringService.scoreMatch(matchId);
    }

    @PostMapping("/run")
    @Operation(summary = "Run automatic scoring cycle")
    @ApiResponse(responseCode = "200", description = "Automatic scoring cycle completed")
    public AutomaticScoringRunResponse runAutomaticScoring() {
        AutomaticScoringRunResult result = automaticScoringService.runOnce();
        return new AutomaticScoringRunResponse(
                result.asOf(),
                result.candidates(),
                result.processed(),
                result.failed()
        );
    }
}
