package com.mundialpolla.stages.infrastructure.persistence;

import com.mundialpolla.stages.domain.Stage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StageRepository extends JpaRepository<Stage, UUID> {

    Optional<Stage> findByExternalId(Long externalId);

    List<Stage> findByTournamentIdOrderByOrderNumberAsc(UUID tournamentId);

    boolean existsByExternalId(Long externalId);
}
