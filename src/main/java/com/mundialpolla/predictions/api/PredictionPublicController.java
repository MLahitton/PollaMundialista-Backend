package com.mundialpolla.predictions.api;

import com.mundialpolla.predictions.application.PredictionPublicQueryService;
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
@RequestMapping("/api/v1/public-predictions")
@Tag(name = "Public Predictions")
public class PredictionPublicController {

    private final PredictionPublicQueryService predictionPublicQueryService;

    public PredictionPublicController(PredictionPublicQueryService predictionPublicQueryService) {
        this.predictionPublicQueryService = predictionPublicQueryService;
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "List visible predictions for a match")
    @ApiResponse(responseCode = "200", description = "Visible predictions found")
    @ApiResponse(responseCode = "404", description = "Participant or match not found")
    public List<PublicPredictionResponse> findPublicPredictionsByMatch(
            @PathVariable UUID matchId,
            @RequestParam UUID requestingParticipantId
    ) {
        return predictionPublicQueryService.findPublicPredictionsByMatch(requestingParticipantId, matchId);
    }

    @GetMapping("/matches/{matchId}/participants/{targetParticipantId}")
    @Operation(summary = "Get a visible prediction for a match participant")
    @ApiResponse(responseCode = "200", description = "Prediction found")
    @ApiResponse(responseCode = "403", description = "Prediction is private")
    @ApiResponse(responseCode = "404", description = "Participant, match, or prediction not found")
    public PublicPredictionResponse findParticipantPredictionForMatch(
            @PathVariable UUID matchId,
            @PathVariable UUID targetParticipantId,
            @RequestParam UUID requestingParticipantId
    ) {
        return predictionPublicQueryService.findParticipantPredictionForMatch(
                requestingParticipantId,
                targetParticipantId,
                matchId
        );
    }
}
