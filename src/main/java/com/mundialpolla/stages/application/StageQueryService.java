package com.mundialpolla.stages.application;

import com.mundialpolla.stages.api.StageResponse;
import com.mundialpolla.stages.domain.Stage;
import com.mundialpolla.stages.infrastructure.persistence.StageRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class StageQueryService {

    private final StageRepository stageRepository;

    public StageQueryService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    public List<StageResponse> findByTournamentId(UUID tournamentId) {
        return stageRepository.findByTournamentIdOrderByOrderNumberAsc(tournamentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public StageResponse findById(UUID id) {
        return stageRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));
    }

    private StageResponse toResponse(Stage stage) {
        return new StageResponse(
                stage.getId(),
                stage.getTournament().getId(),
                stage.getExternalId(),
                stage.getName(),
                stage.getType(),
                stage.getOrderNumber(),
                stage.isActive()
        );
    }
}
