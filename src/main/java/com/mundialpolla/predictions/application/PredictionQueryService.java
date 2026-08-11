package com.mundialpolla.predictions.application;

import com.mundialpolla.predictions.api.PredictionResponse;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.predictions.infrastructure.persistence.PredictionRepository;
import com.mundialpolla.shared.time.ApplicationClock;
import com.mundialpolla.matches.domain.Match;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PredictionQueryService {

    private final PredictionRepository predictionRepository;
    private final ApplicationClock applicationClock;

    public PredictionQueryService(PredictionRepository predictionRepository, ApplicationClock applicationClock) {
        this.predictionRepository = predictionRepository;
        this.applicationClock = applicationClock;
    }

    public PredictionResponse findByParticipantAndMatch(UUID participantId, UUID matchId) {
        Prediction prediction = predictionRepository.findByParticipantIdAndMatchId(participantId, matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prediction not found"));
        return toResponse(prediction);
    }

    public List<PredictionResponse> findByParticipant(UUID participantId) {
        return map(predictionRepository.findByParticipantIdOrderByMatchStartsAtAsc(participantId));
    }

    public List<PredictionResponse> findByParticipantAndTournament(UUID participantId, UUID tournamentId) {
        return map(predictionRepository.findByParticipantIdAndMatchTournamentIdOrderByMatchStartsAtAsc(
                participantId,
                tournamentId
        ));
    }

    private List<PredictionResponse> map(List<Prediction> predictions) {
        Instant now = applicationClock.now();
        return predictions.stream()
                .map(prediction -> toResponse(prediction, now))
                .toList();
    }

    private PredictionResponse toResponse(Prediction prediction) {
        return toResponse(prediction, applicationClock.now());
    }

    private PredictionResponse toResponse(Prediction prediction, Instant now) {
        Match match = prediction.getMatch();
        boolean editable = now.isBefore(match.getPredictionClosesAt()) && prediction.getLockedAt() == null;

        return new PredictionResponse(
                prediction.getId(),
                prediction.getParticipant().getId(),
                match.getId(),
                match.getTournament().getId(),
                match.getStage().getId(),
                match.getGroup() == null ? null : match.getGroup().getId(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getStartsAt(),
                match.getPredictionClosesAt(),
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore(),
                prediction.getPredictedQualifiedTeam() == null ? null : prediction.getPredictedQualifiedTeam().getId(),
                prediction.getSubmittedAt(),
                prediction.getUpdatedAt(),
                prediction.getLockedAt(),
                editable,
                !editable
        );
    }
}
