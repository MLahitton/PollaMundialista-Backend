package com.mundialpolla.me.api;

import com.mundialpolla.auth.api.AuthenticatedParticipantResponse;
import com.mundialpolla.auth.application.CurrentParticipantProvider;
import com.mundialpolla.participants.domain.Participant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me")
@SecurityRequirement(name = "bearerAuth")
public class MeProfileController {

    private final CurrentParticipantProvider currentParticipantProvider;

    public MeProfileController(CurrentParticipantProvider currentParticipantProvider) {
        this.currentParticipantProvider = currentParticipantProvider;
    }

    @GetMapping
    @Operation(summary = "Get my participant profile")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "401", description = "JWT missing or invalid")
    @ApiResponse(responseCode = "403", description = "Participant account is inactive")
    public AuthenticatedParticipantResponse me() {
        Participant participant = currentParticipantProvider.requireCurrentParticipant();
        return new AuthenticatedParticipantResponse(
                participant.getId(),
                participant.getEmail(),
                participant.getDisplayName(),
                participant.getProfileImageUrl(),
                participant.isActive()
        );
    }
}
