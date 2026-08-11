package com.mundialpolla.groups.infrastructure.persistence;

import com.mundialpolla.groups.domain.TournamentGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentGroupRepository extends JpaRepository<TournamentGroup, UUID> {

    Optional<TournamentGroup> findByExternalId(Long externalId);

    Optional<TournamentGroup> findByTournamentIdAndCodeIgnoreCase(UUID tournamentId, String code);

    List<TournamentGroup> findByTournamentIdOrderByOrderNumberAsc(UUID tournamentId);

    List<TournamentGroup> findByStageIdOrderByOrderNumberAsc(UUID stageId);

    boolean existsByExternalId(Long externalId);
}
