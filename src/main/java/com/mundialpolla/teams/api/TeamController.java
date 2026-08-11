package com.mundialpolla.teams.api;

import com.mundialpolla.teams.application.TeamQueryService;
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
@RequestMapping("/api/v1/teams")
@Tag(name = "Teams")
public class TeamController {

    private final TeamQueryService teamQueryService;

    public TeamController(TeamQueryService teamQueryService) {
        this.teamQueryService = teamQueryService;
    }

    @GetMapping
    @Operation(summary = "List teams")
    @ApiResponse(responseCode = "200", description = "Teams found")
    public List<TeamResponse> findAll() {
        return teamQueryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by id")
    @ApiResponse(responseCode = "200", description = "Team found")
    @ApiResponse(responseCode = "404", description = "Team not found")
    public TeamResponse findById(@PathVariable UUID id) {
        return teamQueryService.findById(id);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get team by code")
    @ApiResponse(responseCode = "200", description = "Team found")
    @ApiResponse(responseCode = "404", description = "Team not found")
    public TeamResponse findByCode(@PathVariable String code) {
        return teamQueryService.findByCode(code);
    }
}
