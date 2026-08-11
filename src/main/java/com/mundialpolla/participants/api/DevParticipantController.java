package com.mundialpolla.participants.api;

import com.mundialpolla.participants.application.DevParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/dev/participants")
@Tag(name = "Dev Participants")
public class DevParticipantController {

    private final DevParticipantService devParticipantService;

    public DevParticipantController(DevParticipantService devParticipantService) {
        this.devParticipantService = devParticipantService;
    }

    @PostMapping
    @Operation(summary = "Create a temporary development participant")
    @ApiResponse(responseCode = "200", description = "Development participant returned")
    public ParticipantDevResponse create(@Valid @RequestBody CreateDevParticipantRequest request) {
        return devParticipantService.create(request);
    }
}
