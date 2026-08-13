package com.mundialpolla.me.api;

import com.mundialpolla.auth.application.CurrentParticipantProvider;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.scoring.api.PredictionScoreResponse;
import com.mundialpolla.scoring.application.PredictionScoreQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/scores")
@Tag(name = "My Scores")
@SecurityRequirement(name = "bearerAuth")
public class MePredictionScoreController {

    private final CurrentParticipantProvider currentParticipantProvider;
    private final PredictionScoreQueryService predictionScoreQueryService;

    public MePredictionScoreController(
            CurrentParticipantProvider currentParticipantProvider,
            PredictionScoreQueryService predictionScoreQueryService
    ) {
        this.currentParticipantProvider = currentParticipantProvider;
        this.predictionScoreQueryService = predictionScoreQueryService;
    }

    @GetMapping
    @Operation(summary = "List my prediction scores")
    @ApiResponse(responseCode = "200", description = "Prediction scores found")
    public List<PredictionScoreResponse> findMine(@RequestParam(required = false) UUID tournamentId) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        if (tournamentId != null) {
            return predictionScoreQueryService.findByParticipantAndTournament(participant.getId(), tournamentId);
        }
        return predictionScoreQueryService.findByParticipantId(participant.getId());
    }

    @GetMapping("/predictions/{predictionId}")
    @Operation(summary = "Get my score for a prediction")
    @ApiResponse(responseCode = "200", description = "Prediction score found")
    @ApiResponse(responseCode = "404", description = "Prediction score not found")
    public PredictionScoreResponse findMyPredictionScore(@PathVariable UUID predictionId) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        return predictionScoreQueryService.findByPredictionIdAndParticipantId(predictionId, participant.getId());
    }
}
