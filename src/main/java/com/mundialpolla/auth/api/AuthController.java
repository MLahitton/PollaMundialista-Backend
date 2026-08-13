package com.mundialpolla.auth.api;

import com.mundialpolla.auth.application.AuthenticationService;
import com.mundialpolla.auth.application.CurrentParticipantProvider;
import com.mundialpolla.participants.domain.Participant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CurrentParticipantProvider currentParticipantProvider;

    public AuthController(
            AuthenticationService authenticationService,
            CurrentParticipantProvider currentParticipantProvider
    ) {
        this.authenticationService = authenticationService;
        this.currentParticipantProvider = currentParticipantProvider;
    }

    @PostMapping("/google")
    @Operation(summary = "Exchange Google ID token for application JWT")
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid Google ID token")
    @ApiResponse(responseCode = "403", description = "Participant account is inactive")
    @ApiResponse(responseCode = "409", description = "Email conflict")
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authenticationService.loginWithGoogle(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get authenticated participant",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Authenticated participant found")
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
