package com.mundialpolla.groups.application;

import com.mundialpolla.groups.api.GroupTeamResponse;
import com.mundialpolla.groups.api.TournamentGroupResponse;
import com.mundialpolla.groups.domain.GroupTeam;
import com.mundialpolla.groups.domain.TournamentGroup;
import com.mundialpolla.groups.infrastructure.persistence.GroupTeamRepository;
import com.mundialpolla.groups.infrastructure.persistence.TournamentGroupRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class GroupQueryService {

    private final TournamentGroupRepository tournamentGroupRepository;
    private final GroupTeamRepository groupTeamRepository;

    public GroupQueryService(
            TournamentGroupRepository tournamentGroupRepository,
            GroupTeamRepository groupTeamRepository
    ) {
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.groupTeamRepository = groupTeamRepository;
    }

    public List<TournamentGroupResponse> findByTournamentId(UUID tournamentId) {
        return tournamentGroupRepository.findByTournamentIdOrderByOrderNumberAsc(tournamentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TournamentGroupResponse> findByStageId(UUID stageId) {
        return tournamentGroupRepository.findByStageIdOrderByOrderNumberAsc(stageId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TournamentGroupResponse findById(UUID id) {
        return tournamentGroupRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    public List<GroupTeamResponse> findTeamsByGroupId(UUID groupId) {
        if (!tournamentGroupRepository.existsById(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }

        return groupTeamRepository.findByGroupIdOrderByOrderNumberAsc(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TournamentGroupResponse toResponse(TournamentGroup group) {
        return new TournamentGroupResponse(
                group.getId(),
                group.getTournament().getId(),
                group.getStage().getId(),
                group.getExternalId(),
                group.getName(),
                group.getCode(),
                group.getOrderNumber()
        );
    }

    private GroupTeamResponse toResponse(GroupTeam groupTeam) {
        return new GroupTeamResponse(
                groupTeam.getId(),
                groupTeam.getGroup().getId(),
                groupTeam.getTeam().getId(),
                groupTeam.getTeam().getName(),
                groupTeam.getTeam().getCode(),
                groupTeam.getTeam().getLogoUrl(),
                groupTeam.getOrderNumber()
        );
    }
}
