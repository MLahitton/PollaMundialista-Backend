package com.mundialpolla.scoring.api;

import com.mundialpolla.scoring.application.PredictionScoreQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prediction-scores")
@Tag(name = "Prediction Scores")
public class PredictionScoreController {

    private final PredictionScoreQueryService predictionScoreQueryService;

    public PredictionScoreController(PredictionScoreQueryService predictionScoreQueryService) {
        this.predictionScoreQueryService = predictionScoreQueryService;
    }

    @GetMapping("/predictions/{predictionId}")
    @Operation(summary = "Get score by prediction")
    @ApiResponse(responseCode = "200", description = "Prediction score found")
    @ApiResponse(responseCode = "404", description = "Prediction score not found")
    public PredictionScoreResponse findByPredictionId(@PathVariable UUID predictionId) {
        return predictionScoreQueryService.findByPredictionId(predictionId);
    }

    @GetMapping("/participants/{participantId}")
    @Operation(summary = "List scores by participant")
    @ApiResponse(responseCode = "200", description = "Prediction scores found")
    public List<PredictionScoreResponse> findByParticipant(
            @PathVariable UUID participantId,
            @RequestParam(required = false) UUID tournamentId
    ) {
        if (tournamentId != null) {
            return predictionScoreQueryService.findByParticipantAndTournament(participantId, tournamentId);
        }
        return predictionScoreQueryService.findByParticipantId(participantId);
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "List scores by match")
    @ApiResponse(responseCode = "200", description = "Prediction scores found")
    public List<PredictionScoreResponse> findByMatchId(@PathVariable UUID matchId) {
        return predictionScoreQueryService.findByMatchId(matchId);
    }
}
