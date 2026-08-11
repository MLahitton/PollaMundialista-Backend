package com.mundialpolla.tournaments.application;

import com.mundialpolla.tournaments.api.TournamentResponse;
import com.mundialpolla.tournaments.domain.Tournament;
import com.mundialpolla.tournaments.infrastructure.persistence.TournamentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class TournamentQueryService {

    private final TournamentRepository tournamentRepository;

    public TournamentQueryService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public List<TournamentResponse> findAll() {
        return tournamentRepository.findAllByOrderByStartDateAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public TournamentResponse findById(UUID id) {
        return tournamentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));
    }

    public TournamentResponse findActive() {
        return tournamentRepository.findByActiveTrue()
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active tournament not found"));
    }

    private TournamentResponse toResponse(Tournament tournament) {
        return new TournamentResponse(
                tournament.getId(),
                tournament.getExternalId(),
                tournament.getName(),
                tournament.getSeason(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.isActive()
        );
    }
}
