package com.mundialpolla.scoring.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.infrastructure.persistence.MatchRepository;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.predictions.infrastructure.persistence.PredictionRepository;
import com.mundialpolla.scoring.api.MatchScoringResultResponse;
import com.mundialpolla.scoring.domain.PredictionScore;
import com.mundialpolla.scoring.infrastructure.persistence.PredictionScoreRepository;
import com.mundialpolla.shared.time.ApplicationClock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MatchScoringService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionScoreRepository predictionScoreRepository;
    private final PredictionScoringCalculator predictionScoringCalculator;
    private final ApplicationClock applicationClock;

    public MatchScoringService(
            MatchRepository matchRepository,
            PredictionRepository predictionRepository,
            PredictionScoreRepository predictionScoreRepository,
            PredictionScoringCalculator predictionScoringCalculator,
            ApplicationClock applicationClock
    ) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.predictionScoreRepository = predictionScoreRepository;
        this.predictionScoringCalculator = predictionScoringCalculator;
        this.applicationClock = applicationClock;
    }

    public MatchScoringResultResponse scoreMatch(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        if (match.getHomeScore() == null || match.getAwayScore() == null || match.getResultConfirmedAt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Match result is not confirmed");
        }

        Instant now = applicationClock.now();
        if (now.isBefore(match.getStartsAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Match has not started yet");
        }

        List<Prediction> predictions = predictionRepository.findByMatchId(matchId);
        ScoreCounters counters = new ScoreCounters();
        for (Prediction prediction : predictions) {
            PredictionScoreCalculation calculation = predictionScoringCalculator.calculate(prediction);
            predictionScoreRepository.findByPredictionId(prediction.getId())
                    .ifPresentOrElse(existing -> {
                        existing.updateScore(
                                calculation.basePoints(),
                                calculation.qualifiedTeamBonus(),
                                calculation.exactScore(),
                                calculation.correctOutcome(),
                                calculation.correctQualifiedTeam(),
                                now
                        );
                        counters.scoresUpdated++;
                    }, () -> {
                        predictionScoreRepository.save(new PredictionScore(
                                prediction,
                                prediction.getParticipant(),
                                match,
                                calculation.basePoints(),
                                calculation.qualifiedTeamBonus(),
                                calculation.exactScore(),
                                calculation.correctOutcome(),
                                calculation.correctQualifiedTeam(),
                                now
                        ));
                        counters.scoresCreated++;
                    });
            counters.predictionsProcessed++;
        }

        Instant matchScoredAt = match.getScoredAt();
        if (matchScoredAt == null) {
            match.markScored(now);
            matchScoredAt = now;
        }

        return new MatchScoringResultResponse(
                match.getId(),
                counters.predictionsProcessed,
                counters.scoresCreated,
                counters.scoresUpdated,
                matchScoredAt
        );
    }

    private static final class ScoreCounters {
        private int predictionsProcessed;
        private int scoresCreated;
        private int scoresUpdated;
    }
}
