package com.mundialpolla.groups.api;

import com.mundialpolla.groups.application.GroupQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/groups")
@Tag(name = "Groups")
public class TournamentGroupController {

    private final GroupQueryService groupQueryService;

    public TournamentGroupController(GroupQueryService groupQueryService) {
        this.groupQueryService = groupQueryService;
    }

    @GetMapping
    @Operation(summary = "List groups by tournament or stage")
    @ApiResponse(responseCode = "200", description = "Groups found")
    @ApiResponse(responseCode = "400", description = "Invalid filter combination")
    public List<TournamentGroupResponse> findGroups(
            @RequestParam(required = false) UUID tournamentId,
            @RequestParam(required = false) UUID stageId
    ) {
        if ((tournamentId == null && stageId == null) || (tournamentId != null && stageId != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Exactly one of tournamentId or stageId must be provided"
            );
        }

        return tournamentId != null
                ? groupQueryService.findByTournamentId(tournamentId)
                : groupQueryService.findByStageId(stageId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by id")
    @ApiResponse(responseCode = "200", description = "Group found")
    @ApiResponse(responseCode = "404", description = "Group not found")
    public TournamentGroupResponse findById(@PathVariable UUID id) {
        return groupQueryService.findById(id);
    }

    @GetMapping("/{id}/teams")
    @Operation(summary = "List teams assigned to a group")
    @ApiResponse(responseCode = "200", description = "Group teams found")
    @ApiResponse(responseCode = "404", description = "Group not found")
    public List<GroupTeamResponse> findTeamsByGroupId(@PathVariable UUID id) {
        return groupQueryService.findTeamsByGroupId(id);
    }
}
