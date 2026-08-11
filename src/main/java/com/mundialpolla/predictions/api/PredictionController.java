package com.mundialpolla.predictions.api;

import com.mundialpolla.predictions.application.PredictionQueryService;
import com.mundialpolla.predictions.application.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/v1/predictions")
@Tag(name = "Predictions")
public class PredictionController {

    private final PredictionService predictionService;
    private final PredictionQueryService predictionQueryService;

    public PredictionController(
            PredictionService predictionService,
            PredictionQueryService predictionQueryService
    ) {
        this.predictionService = predictionService;
        this.predictionQueryService = predictionQueryService;
    }

    @PutMapping("/participants/{participantId}/matches/{matchId}")
    @Operation(summary = "Create or update a prediction")
    @ApiResponse(responseCode = "200", description = "Prediction saved")
    @ApiResponse(responseCode = "400", description = "Invalid prediction")
    @ApiResponse(responseCode = "404", description = "Participant, match, or team not found")
    @ApiResponse(responseCode = "409", description = "Prediction is closed or locked")
    public PredictionResponse upsertPrediction(
            @PathVariable UUID participantId,
            @PathVariable UUID matchId,
            @Valid @RequestBody UpsertPredictionRequest request
    ) {
        return predictionService.upsertPrediction(participantId, matchId, request);
    }

    @GetMapping("/participants/{participantId}/matches/{matchId}")
    @Operation(summary = "Get a prediction by participant and match")
    @ApiResponse(responseCode = "200", description = "Prediction found")
    @ApiResponse(responseCode = "404", description = "Prediction not found")
    public PredictionResponse findByParticipantAndMatch(
            @PathVariable UUID participantId,
            @PathVariable UUID matchId
    ) {
        return predictionQueryService.findByParticipantAndMatch(participantId, matchId);
    }

    @GetMapping("/participants/{participantId}")
    @Operation(summary = "List predictions by participant")
    @ApiResponse(responseCode = "200", description = "Predictions found")
    public List<PredictionResponse> findByParticipant(
            @PathVariable UUID participantId,
            @RequestParam(required = false) UUID tournamentId
    ) {
        if (tournamentId != null) {
            return predictionQueryService.findByParticipantAndTournament(participantId, tournamentId);
        }
        return predictionQueryService.findByParticipant(participantId);
    }
}
