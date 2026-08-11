package com.mundialpolla.groups.infrastructure.persistence;

import com.mundialpolla.groups.domain.GroupTeam;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupTeamRepository extends JpaRepository<GroupTeam, UUID> {

    List<GroupTeam> findByGroupIdOrderByOrderNumberAsc(UUID groupId);

    Optional<GroupTeam> findByGroupIdAndTeamId(UUID groupId, UUID teamId);

    boolean existsByGroupIdAndTeamId(UUID groupId, UUID teamId);

    boolean existsByTeamId(UUID teamId);
}
