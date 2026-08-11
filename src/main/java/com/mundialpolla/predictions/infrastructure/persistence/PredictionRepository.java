package com.mundialpolla.predictions.infrastructure.persistence;

import com.mundialpolla.predictions.domain.Prediction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionRepository extends JpaRepository<Prediction, UUID> {

    Optional<Prediction> findByParticipantIdAndMatchId(UUID participantId, UUID matchId);

    List<Prediction> findByParticipantIdOrderByMatchStartsAtAsc(UUID participantId);

    List<Prediction> findByParticipantIdAndMatchTournamentIdOrderByMatchStartsAtAsc(
            UUID participantId,
            UUID tournamentId
    );

    List<Prediction> findByMatchId(UUID matchId);

    boolean existsByParticipantIdAndMatchId(UUID participantId, UUID matchId);
}
