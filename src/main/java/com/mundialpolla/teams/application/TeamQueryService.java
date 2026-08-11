package com.mundialpolla.teams.application;

import com.mundialpolla.teams.api.TeamResponse;
import com.mundialpolla.teams.domain.Team;
import com.mundialpolla.teams.infrastructure.persistence.TeamRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamRepository teamRepository;

    public TeamQueryService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<TeamResponse> findAll() {
        return teamRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public TeamResponse findById(UUID id) {
        return teamRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    public TeamResponse findByCode(String code) {
        return teamRepository.findByCodeIgnoreCase(code.trim())
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getExternalId(),
                team.getName(),
                team.getShortName(),
                team.getCode(),
                team.getCountryCode(),
                team.getLogoUrl(),
                team.isActive()
        );
    }
}
