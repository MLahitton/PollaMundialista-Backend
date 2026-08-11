package com.mundialpolla.tournaments.infrastructure.persistence;

import com.mundialpolla.tournaments.domain.Tournament;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    Optional<Tournament> findByExternalId(Long externalId);

    Optional<Tournament> findByActiveTrue();

    List<Tournament> findAllByOrderByStartDateAsc();

    boolean existsByExternalId(Long externalId);
}
