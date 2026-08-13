package com.mundialpolla.scoring.infrastructure.persistence;

import com.mundialpolla.scoring.domain.PredictionScore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionScoreRepository extends JpaRepository<PredictionScore, UUID> {

    Optional<PredictionScore> findByPredictionId(UUID predictionId);

    Optional<PredictionScore> findByPredictionIdAndParticipantId(UUID predictionId, UUID participantId);

    List<PredictionScore> findByParticipantIdOrderByMatchStartsAtAsc(UUID participantId);

    List<PredictionScore> findByParticipantIdAndMatchTournamentIdOrderByMatchStartsAtAsc(
            UUID participantId,
            UUID tournamentId
    );

    List<PredictionScore> findByMatchTournamentIdAndMatchScoredAtLessThanEqual(UUID tournamentId, Instant scoredAt);

    List<PredictionScore> findByMatchId(UUID matchId);

    boolean existsByPredictionId(UUID predictionId);
}
