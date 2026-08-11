package com.mundialpolla.tournaments.api;

import com.mundialpolla.tournaments.application.TournamentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tournaments")
@Tag(name = "Tournaments")
public class TournamentController {

    private final TournamentQueryService tournamentQueryService;

    public TournamentController(TournamentQueryService tournamentQueryService) {
        this.tournamentQueryService = tournamentQueryService;
    }

    @GetMapping
    @Operation(summary = "List tournaments")
    @ApiResponse(responseCode = "200", description = "Tournaments found")
    public List<TournamentResponse> findAll() {
        return tournamentQueryService.findAll();
    }

    @GetMapping("/active")
    @Operation(summary = "Get active tournament")
    @ApiResponse(responseCode = "200", description = "Active tournament found")
    @ApiResponse(responseCode = "404", description = "Active tournament not found")
    public TournamentResponse findActive() {
        return tournamentQueryService.findActive();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tournament by id")
    @ApiResponse(responseCode = "200", description = "Tournament found")
    @ApiResponse(responseCode = "404", description = "Tournament not found")
    public TournamentResponse findById(@PathVariable UUID id) {
        return tournamentQueryService.findById(id);
    }
}
