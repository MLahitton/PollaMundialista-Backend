package com.mundialpolla.ranking.application;

import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.participants.infrastructure.persistence.ParticipantRepository;
import com.mundialpolla.ranking.api.RankingEntryResponse;
import com.mundialpolla.ranking.api.TournamentRankingResponse;
import com.mundialpolla.ranking.domain.ParticipantRankingSnapshot;
import com.mundialpolla.scoring.domain.PredictionScore;
import com.mundialpolla.scoring.infrastructure.persistence.PredictionScoreRepository;
import com.mundialpolla.shared.time.ApplicationClock;
import com.mundialpolla.tournaments.infrastructure.persistence.TournamentRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class RankingService {

    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;
    private final PredictionScoreRepository predictionScoreRepository;
    private final ApplicationClock applicationClock;

    public RankingService(
            TournamentRepository tournamentRepository,
            ParticipantRepository participantRepository,
            PredictionScoreRepository predictionScoreRepository,
            ApplicationClock applicationClock
    ) {
        this.tournamentRepository = tournamentRepository;
        this.participantRepository = participantRepository;
        this.predictionScoreRepository = predictionScoreRepository;
        this.applicationClock = applicationClock;
    }

    public TournamentRankingResponse getTournamentRanking(UUID tournamentId, UUID currentParticipantId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));
        Participant currentParticipant = participantRepository.findById(currentParticipantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));

        Instant now = applicationClock.now();
        List<PredictionScore> visibleScores =
                predictionScoreRepository.findByMatchTournamentIdAndMatchScoredAtLessThanEqual(tournamentId, now);
        Map<UUID, ParticipantRankingSnapshot> snapshotsByParticipant = aggregateByParticipant(visibleScores);
        List<ParticipantRankingSnapshot> orderedSnapshots = snapshotsByParticipant.values().stream()
                .sorted(snapshotComparator())
                .toList();
        Map<UUID, Long> positionsByParticipant = calculatePositions(orderedSnapshots);

        List<RankingEntryResponse> top10 = orderedSnapshots.stream()
                .limit(10)
                .map(snapshot -> toResponse(
                        snapshot,
                        positionsByParticipant.get(snapshot.participant().getId()),
                        snapshot.participant().getId().equals(currentParticipantId)
                ))
                .toList();

        ParticipantRankingSnapshot currentSnapshot = snapshotsByParticipant.getOrDefault(
                currentParticipantId,
                zeroSnapshot(currentParticipant)
        );
        long currentPosition = positionsByParticipant.getOrDefault(
                currentParticipantId,
                1L + orderedSnapshots.stream()
                        .filter(snapshot -> snapshot.totalPoints() > currentSnapshot.totalPoints())
                        .count()
        );

        return new TournamentRankingResponse(
                tournamentId,
                now,
                top10,
                toResponse(currentSnapshot, currentPosition, true),
                snapshotsByParticipant.size()
        );
    }

    private Map<UUID, ParticipantRankingSnapshot> aggregateByParticipant(List<PredictionScore> scores) {
        Map<UUID, ParticipantRankingSnapshot> snapshotsByParticipant = new HashMap<>();
        for (PredictionScore score : scores) {
            Participant participant = score.getParticipant();
            ParticipantRankingSnapshot existing = snapshotsByParticipant.getOrDefault(
                    participant.getId(),
                    zeroSnapshot(participant)
            );
            snapshotsByParticipant.put(
                    participant.getId(),
                    existing.addScore(
                            score.getTotalPoints(),
                            score.isExactScore(),
                            score.isCorrectOutcome(),
                            score.getQualifiedTeamBonus()
                    )
            );
        }
        return snapshotsByParticipant;
    }

    private Map<UUID, Long> calculatePositions(List<ParticipantRankingSnapshot> orderedSnapshots) {
        Map<UUID, Long> positionsByParticipant = new HashMap<>();
        long currentPosition = 0;
        Integer previousPoints = null;
        for (int index = 0; index < orderedSnapshots.size(); index++) {
            ParticipantRankingSnapshot snapshot = orderedSnapshots.get(index);
            if (previousPoints == null || snapshot.totalPoints() != previousPoints) {
                currentPosition = index + 1L;
                previousPoints = snapshot.totalPoints();
            }
            positionsByParticipant.put(snapshot.participant().getId(), currentPosition);
        }
        return positionsByParticipant;
    }

    private Comparator<ParticipantRankingSnapshot> snapshotComparator() {
        return Comparator.comparingInt(ParticipantRankingSnapshot::totalPoints)
                .reversed()
                .thenComparing(
                        snapshot -> snapshot.participant().getDisplayName(),
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(snapshot -> snapshot.participant().getId());
    }

    private ParticipantRankingSnapshot zeroSnapshot(Participant participant) {
        return new ParticipantRankingSnapshot(participant, 0, 0, 0, 0, 0);
    }

    private RankingEntryResponse toResponse(
            ParticipantRankingSnapshot snapshot,
            long position,
            boolean currentParticipant
    ) {
        Participant participant = snapshot.participant();
        return new RankingEntryResponse(
                participant.getId(),
                participant.getDisplayName(),
                participant.getProfileImageUrl(),
                position,
                snapshot.totalPoints(),
                snapshot.exactScores(),
                snapshot.correctOutcomes(),
                snapshot.qualifiedTeamBonuses(),
                snapshot.scoredPredictions(),
                currentParticipant
        );
    }
}
