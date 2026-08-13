package com.mundialpolla.predictions.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.infrastructure.persistence.MatchRepository;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.participants.infrastructure.persistence.ParticipantRepository;
import com.mundialpolla.predictions.api.PublicPredictionResponse;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.predictions.infrastructure.persistence.PredictionRepository;
import com.mundialpolla.shared.time.ApplicationClock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PredictionPublicQueryService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final PredictionVisibilityResolver predictionVisibilityResolver;
    private final ApplicationClock applicationClock;

    public PredictionPublicQueryService(
            PredictionRepository predictionRepository,
            MatchRepository matchRepository,
            ParticipantRepository participantRepository,
            PredictionVisibilityResolver predictionVisibilityResolver,
            ApplicationClock applicationClock
    ) {
        this.predictionRepository = predictionRepository;
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.predictionVisibilityResolver = predictionVisibilityResolver;
        this.applicationClock = applicationClock;
    }

    public List<PublicPredictionResponse> findPublicPredictionsByMatch(
            UUID requestingParticipantId,
            UUID matchId
    ) {
        participantRepository.findById(requestingParticipantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        Instant now = applicationClock.now();
        List<Prediction> predictions = predictionRepository.findByMatchId(matchId);
        if (now.isBefore(match.getPredictionClosesAt())) {
            predictions = predictions.stream()
                    .filter(prediction -> prediction.getParticipant().getId().equals(requestingParticipantId))
                    .toList();
        }

        return predictions.stream()
                .sorted(predictionComparator())
                .map(this::toResponse)
                .toList();
    }

    public PublicPredictionResponse findParticipantPredictionForMatch(
            UUID requestingParticipantId,
            UUID targetParticipantId,
            UUID matchId
    ) {
        participantRepository.findById(requestingParticipantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
        participantRepository.findById(targetParticipantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
        matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        Prediction prediction = predictionRepository.findByParticipantIdAndMatchId(targetParticipantId, matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prediction not found"));
        Instant now = applicationClock.now();
        if (!requestingParticipantId.equals(targetParticipantId)
                && predictionVisibilityResolver.resolve(prediction, now) == PredictionVisibility.PRIVATE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Prediction is private until the match prediction window closes"
            );
        }

        return toResponse(prediction);
    }

    private Comparator<Prediction> predictionComparator() {
        return Comparator
                .comparing(
                        (Prediction prediction) -> prediction.getParticipant().getDisplayName(),
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(prediction -> prediction.getParticipant().getId());
    }

    private PublicPredictionResponse toResponse(Prediction prediction) {
        Participant participant = prediction.getParticipant();
        return new PublicPredictionResponse(
                prediction.getId(),
                participant.getId(),
                participant.getDisplayName(),
                participant.getProfileImageUrl(),
                prediction.getMatch().getId(),
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore(),
                prediction.getPredictedQualifiedTeam() == null ? null : prediction.getPredictedQualifiedTeam().getId(),
                prediction.getSubmittedAt(),
                prediction.getUpdatedAt()
        );
    }
}
