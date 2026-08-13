package com.mundialpolla.auth.application;

import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.participants.infrastructure.persistence.ParticipantRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentParticipantProvider {

    private final ParticipantRepository participantRepository;

    public CurrentParticipantProvider(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public Participant requireCurrentParticipant() {
        UUID participantId = requireCurrentParticipantId();
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated participant not found"
                ));
        if (!participant.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Participant account is inactive");
        }
        return participant;
    }

    public UUID requireCurrentParticipantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated participant not found");
        }

        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated participant not found");
        }
    }
}
