package com.mundialpolla.participants.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDevParticipantRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(max = 150)
        String displayName
) {
}
