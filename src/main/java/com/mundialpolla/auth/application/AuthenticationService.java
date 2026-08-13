package com.mundialpolla.auth.application;

import com.mundialpolla.auth.api.AuthResponse;
import com.mundialpolla.auth.api.AuthenticatedParticipantResponse;
import com.mundialpolla.auth.api.GoogleLoginRequest;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.participants.infrastructure.persistence.ParticipantRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AuthenticationService {

    private static final String TOKEN_TYPE = "Bearer";

    private final GoogleIdentityService googleIdentityService;
    private final ParticipantRepository participantRepository;
    private final JwtTokenService jwtTokenService;

    public AuthenticationService(
            GoogleIdentityService googleIdentityService,
            ParticipantRepository participantRepository,
            JwtTokenService jwtTokenService
    ) {
        this.googleIdentityService = googleIdentityService;
        this.participantRepository = participantRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdentity googleIdentity = googleIdentityService.verify(request.idToken());
        Participant participant = resolveParticipant(googleIdentity);
        if (!participant.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Participant account is inactive");
        }

        ensureEmailIsNotAssociatedWithAnotherParticipant(googleIdentity.email(), participant);
        participant.updateProfile(
                googleIdentity.email(),
                displayName(googleIdentity),
                googleIdentity.profileImageUrl()
        );
        participant.registerLogin();
        IssuedJwt issuedJwt = jwtTokenService.issueToken(participant);

        return new AuthResponse(
                issuedJwt.token(),
                TOKEN_TYPE,
                issuedJwt.expiresAt(),
                toResponse(participant)
        );
    }

    private Participant resolveParticipant(GoogleIdentity googleIdentity) {
        return participantRepository.findByGoogleSubject(googleIdentity.subject())
                .orElseGet(() -> resolveParticipantWithoutGoogleSubject(googleIdentity));
    }

    private Participant resolveParticipantWithoutGoogleSubject(GoogleIdentity googleIdentity) {
        return participantRepository.findByEmailIgnoreCase(googleIdentity.email())
                .map(existing -> {
                    if (!existing.getGoogleSubject().equals(googleIdentity.subject())) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Email is already associated with another Google account"
                        );
                    }
                    return existing;
                })
                .orElseGet(() -> participantRepository.save(newParticipant(googleIdentity)));
    }

    private Participant newParticipant(GoogleIdentity googleIdentity) {
        Participant participant = new Participant(
                googleIdentity.subject(),
                googleIdentity.email(),
                displayName(googleIdentity),
                googleIdentity.profileImageUrl()
        );
        participant.registerLogin();
        return participant;
    }

    private void ensureEmailIsNotAssociatedWithAnotherParticipant(String email, Participant participant) {
        participantRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(participant.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email is already associated with another Google account"
                    );
                });
    }

    private String displayName(GoogleIdentity googleIdentity) {
        if (googleIdentity.displayName() != null && !googleIdentity.displayName().isBlank()) {
            return googleIdentity.displayName();
        }

        String normalizedEmail = googleIdentity.email().trim().toLowerCase(Locale.ROOT);
        int atIndex = normalizedEmail.indexOf('@');
        return atIndex > 0 ? normalizedEmail.substring(0, atIndex) : normalizedEmail;
    }

    private AuthenticatedParticipantResponse toResponse(Participant participant) {
        return new AuthenticatedParticipantResponse(
                participant.getId(),
                participant.getEmail(),
                participant.getDisplayName(),
                participant.getProfileImageUrl(),
                participant.isActive()
        );
    }
}
