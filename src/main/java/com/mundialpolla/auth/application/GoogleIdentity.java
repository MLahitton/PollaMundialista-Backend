package com.mundialpolla.auth.application;

public record GoogleIdentity(
        String subject,
        String email,
        String displayName,
        String profileImageUrl
) {
}
