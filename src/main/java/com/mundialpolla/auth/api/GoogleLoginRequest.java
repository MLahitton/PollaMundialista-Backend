package com.mundialpolla.auth.api;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank
        String idToken
) {
}
