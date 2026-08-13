package com.mundialpolla.matches.api;

import com.mundialpolla.matches.application.MatchQueryService;
import com.mundialpolla.matches.domain.MatchStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/matches")
@Tag(name = "Matches")
public class MatchController {

    private final MatchQueryService matchQueryService;

    public MatchController(MatchQueryService matchQueryService) {
        this.matchQueryService = matchQueryService;
    }

    @GetMapping
    @Operation(summary = "List matches by filter. The status filter uses persisted match status; response status is projected.")
    @ApiResponse(responseCode = "200", description = "Matches found")
    @ApiResponse(responseCode = "400", description = "Invalid filter")
    public List<MatchResponse> findMatches(
            @RequestParam(required = false) UUID tournamentId,
            @RequestParam(required = false) UUID stageId,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        boolean hasRangePart = from != null || to != null;
        if (hasRangePart && (from == null || to == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both from and to must be provided for date range filtering"
            );
        }
        if (from != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before or equal to to");
        }

        int filterCount = countProvided(tournamentId != null)
                + countProvided(stageId != null)
                + countProvided(groupId != null)
                + countProvided(teamId != null)
                + countProvided(status != null)
                + countProvided(hasRangePart);
        if (filterCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exactly one match filter must be provided");
        }

        if (tournamentId != null) {
            return matchQueryService.findByTournamentId(tournamentId);
        }
        if (stageId != null) {
            return matchQueryService.findByStageId(stageId);
        }
        if (groupId != null) {
            return matchQueryService.findByGroupId(groupId);
        }
        if (teamId != null) {
            return matchQueryService.findByTeamId(teamId);
        }
        if (status != null) {
            return matchQueryService.findByStatus(status);
        }

        return matchQueryService.findBetween(from, to);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming matches by tournament")
    @ApiResponse(responseCode = "200", description = "Upcoming matches found")
    public List<MatchResponse> findUpcoming(@RequestParam UUID tournamentId) {
        return matchQueryService.findUpcoming(tournamentId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match by id")
    @ApiResponse(responseCode = "200", description = "Match found")
    @ApiResponse(responseCode = "404", description = "Match not found")
    public MatchResponse findById(@PathVariable UUID id) {
        return matchQueryService.findById(id);
    }

    private int countProvided(boolean provided) {
        return provided ? 1 : 0;
    }
}
