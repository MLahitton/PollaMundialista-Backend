package com.mundialpolla.me.api;

import com.mundialpolla.auth.application.CurrentParticipantProvider;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.predictions.api.PredictionResponse;
import com.mundialpolla.predictions.api.UpsertPredictionRequest;
import com.mundialpolla.predictions.application.PredictionQueryService;
import com.mundialpolla.predictions.application.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/predictions")
@Tag(name = "My Predictions")
@SecurityRequirement(name = "bearerAuth")
public class MePredictionController {

    private final CurrentParticipantProvider currentParticipantProvider;
    private final PredictionService predictionService;
    private final PredictionQueryService predictionQueryService;

    public MePredictionController(
            CurrentParticipantProvider currentParticipantProvider,
            PredictionService predictionService,
            PredictionQueryService predictionQueryService
    ) {
        this.currentParticipantProvider = currentParticipantProvider;
        this.predictionService = predictionService;
        this.predictionQueryService = predictionQueryService;
    }

    @PutMapping("/matches/{matchId}")
    @Operation(summary = "Create or update my prediction")
    @ApiResponse(responseCode = "200", description = "Prediction saved")
    public PredictionResponse upsertPrediction(
            @PathVariable UUID matchId,
            @Valid @RequestBody UpsertPredictionRequest request
    ) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        return predictionService.upsertPrediction(participant.getId(), matchId, request);
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "Get my prediction for a match")
    @ApiResponse(responseCode = "200", description = "Prediction found")
    @ApiResponse(responseCode = "404", description = "Prediction not found")
    public PredictionResponse findByMatch(@PathVariable UUID matchId) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        return predictionQueryService.findByParticipantAndMatch(participant.getId(), matchId);
    }

    @GetMapping
    @Operation(summary = "List my predictions")
    @ApiResponse(responseCode = "200", description = "Predictions found")
    public List<PredictionResponse> findMine(@RequestParam(required = false) UUID tournamentId) {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        if (tournamentId != null) {
            return predictionQueryService.findByParticipantAndTournament(participant.getId(), tournamentId);
        }
        return predictionQueryService.findByParticipant(participant.getId());
    }
}
