package com.mundialpolla.stages.api;

import com.mundialpolla.stages.application.StageQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stages")
@Tag(name = "Stages")
public class StageController {

    private final StageQueryService stageQueryService;

    public StageController(StageQueryService stageQueryService) {
        this.stageQueryService = stageQueryService;
    }

    @GetMapping
    @Operation(summary = "List stages by tournament")
    @ApiResponse(responseCode = "200", description = "Stages found")
    public List<StageResponse> findByTournamentId(@RequestParam UUID tournamentId) {
        return stageQueryService.findByTournamentId(tournamentId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get stage by id")
    @ApiResponse(responseCode = "200", description = "Stage found")
    @ApiResponse(responseCode = "404", description = "Stage not found")
    public StageResponse findById(@PathVariable UUID id) {
        return stageQueryService.findById(id);
    }
}
