package com.mundialpolla.predictions.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.infrastructure.persistence.MatchRepository;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.participants.infrastructure.persistence.ParticipantRepository;
import com.mundialpolla.predictions.api.PredictionResponse;
import com.mundialpolla.predictions.api.UpsertPredictionRequest;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.predictions.infrastructure.persistence.PredictionRepository;
import com.mundialpolla.shared.time.ApplicationClock;
import com.mundialpolla.stages.domain.StageType;
import com.mundialpolla.teams.domain.Team;
import com.mundialpolla.teams.infrastructure.persistence.TeamRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final ParticipantRepository participantRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final ApplicationClock applicationClock;

    public PredictionService(
            PredictionRepository predictionRepository,
            ParticipantRepository participantRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            ApplicationClock applicationClock
    ) {
        this.predictionRepository = predictionRepository;
        this.participantRepository = participantRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.applicationClock = applicationClock;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public PredictionResponse upsertPrediction(
            UUID participantId,
            UUID matchId,
            UpsertPredictionRequest request
    ) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        Instant now = applicationClock.now();
        Optional<Prediction> existingPrediction = predictionRepository.findByParticipantIdAndMatchId(participantId, matchId);
        if (!now.isBefore(match.getPredictionClosesAt())) {
            existingPrediction.ifPresent(existing -> {
                existing.lock(now);
                predictionRepository.save(existing);
            });
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prediction is closed for this match");
        }

        Team predictedQualifiedTeam = resolvePredictedQualifiedTeam(match, request.predictedQualifiedTeamId());
        return existingPrediction
                .map(existing -> updateExistingPrediction(existing, request, predictedQualifiedTeam, now))
                .orElseGet(() -> createPrediction(participant, match, request, predictedQualifiedTeam, now));
    }

    private PredictionResponse createPrediction(
            Participant participant,
            Match match,
            UpsertPredictionRequest request,
            Team predictedQualifiedTeam,
            Instant now
    ) {
        Prediction prediction = new Prediction(
                participant,
                match,
                request.predictedHomeScore(),
                request.predictedAwayScore(),
                predictedQualifiedTeam,
                now
        );
        return toResponse(predictionRepository.save(prediction), now);
    }

    private PredictionResponse updateExistingPrediction(
            Prediction prediction,
            UpsertPredictionRequest request,
            Team predictedQualifiedTeam,
            Instant now
    ) {
        if (prediction.getLockedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prediction is locked");
        }

        prediction.updatePrediction(
                request.predictedHomeScore(),
                request.predictedAwayScore(),
                predictedQualifiedTeam,
                now
        );
        return toResponse(prediction, now);
    }

    private Team resolvePredictedQualifiedTeam(Match match, UUID predictedQualifiedTeamId) {
        if (predictedQualifiedTeamId == null) {
            return null;
        }
        if (match.getStage().getType() == StageType.GROUP_STAGE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Qualified team prediction is not allowed for group-stage matches"
            );
        }

        Team team = teamRepository.findById(predictedQualifiedTeamId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Predicted qualified team not found"
                ));
        if (!isSameTeam(team, match.getHomeTeam()) && !isSameTeam(team, match.getAwayTeam())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Predicted qualified team must belong to the match"
            );
        }
        return team;
    }

    private boolean isSameTeam(Team firstTeam, Team secondTeam) {
        return firstTeam == secondTeam
                || firstTeam != null
                && secondTeam != null
                && Objects.equals(firstTeam.getId(), secondTeam.getId());
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
