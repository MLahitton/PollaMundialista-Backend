package com.mundialpolla.teams.infrastructure.persistence;

import com.mundialpolla.teams.domain.Team;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByExternalId(Long externalId);

    Optional<Team> findByCodeIgnoreCase(String code);

    List<Team> findAllByOrderByNameAsc();

    boolean existsByExternalId(Long externalId);
}
