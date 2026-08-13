package com.mundialpolla.me.api;

import com.mundialpolla.auth.application.CurrentParticipantProvider;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.predictions.api.PublicPredictionResponse;
import com.mundialpolla.predictions.application.PredictionPublicQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/public-predictions")
@Tag(name = "Match Predictions")
@SecurityRequirement(name = "bearerAuth")
public class MePublicPredictionController {

    private final CurrentParticipantProvider currentParticipantProvider;
    private final PredictionPublicQueryService predictionPublicQueryService;

    public MePublicPredictionController(
            CurrentParticipantProvider currentParticipantProvider,
            PredictionPublicQueryService predictionPublicQueryService
    ) {
        this.currentParticipantProvider = currentParticipantProvider;
        this.predictionPublicQueryService = predictionPublicQueryService;
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "List visible predictions for a match as current participant")
    @ApiResponse(responseCode = "200", description = "Visible predictions found")
    public List<PublicPredictionResponse> findPublicPredictionsByMatch(@PathVariable UUID matchId) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        return predictionPublicQueryService.findPublicPredictionsByMatch(participant.getId(), matchId);
    }

    @GetMapping("/matches/{matchId}/participants/{targetParticipantId}")
    @Operation(summary = "Get a visible match prediction as current participant")
    @ApiResponse(responseCode = "200", description = "Prediction found")
    @ApiResponse(responseCode = "403", description = "Prediction is private")
    @ApiResponse(responseCode = "404", description = "Prediction not found")
    public PublicPredictionResponse findParticipantPredictionForMatch(
            @PathVariable UUID matchId,
            @PathVariable UUID targetParticipantId
    ) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        return predictionPublicQueryService.findParticipantPredictionForMatch(
                participant.getId(),
                targetParticipantId,
                matchId
        );
    }
}
