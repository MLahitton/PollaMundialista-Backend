package com.mundialpolla.scoring.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.scoring.api.PredictionScoreResponse;
import com.mundialpolla.scoring.domain.PredictionScore;
import com.mundialpolla.scoring.infrastructure.persistence.PredictionScoreRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PredictionScoreQueryService {

    private final PredictionScoreRepository predictionScoreRepository;

    public PredictionScoreQueryService(PredictionScoreRepository predictionScoreRepository) {
        this.predictionScoreRepository = predictionScoreRepository;
    }

    public PredictionScoreResponse findByPredictionId(UUID predictionId) {
        return predictionScoreRepository.findByPredictionId(predictionId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prediction score not found"));
    }

    public List<PredictionScoreResponse> findByParticipantId(UUID participantId) {
        return map(predictionScoreRepository.findByParticipantIdOrderByMatchStartsAtAsc(participantId));
    }

    public List<PredictionScoreResponse> findByParticipantAndTournament(UUID participantId, UUID tournamentId) {
        return map(predictionScoreRepository.findByParticipantIdAndMatchTournamentIdOrderByMatchStartsAtAsc(
                participantId,
                tournamentId
        ));
    }

    public List<PredictionScoreResponse> findByMatchId(UUID matchId) {
        return map(predictionScoreRepository.findByMatchId(matchId));
    }

    private List<PredictionScoreResponse> map(List<PredictionScore> scores) {
        return scores.stream()
                .map(this::toResponse)
                .toList();
    }

    private PredictionScoreResponse toResponse(PredictionScore score) {
        Prediction prediction = score.getPrediction();
        Match match = score.getMatch();

        return new PredictionScoreResponse(
                score.getId(),
                prediction.getId(),
                score.getParticipant().getId(),
                match.getId(),
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore(),
                match.getHomeScore(),
                match.getAwayScore(),
                prediction.getPredictedQualifiedTeam() == null ? null : prediction.getPredictedQualifiedTeam().getId(),
                match.getQualifiedTeam() == null ? null : match.getQualifiedTeam().getId(),
                score.getBasePoints(),
                score.getQualifiedTeamBonus(),
                score.getTotalPoints(),
                score.isExactScore(),
                score.isCorrectOutcome(),
                score.isCorrectQualifiedTeam(),
                score.getScoredAt()
        );
    }
}
