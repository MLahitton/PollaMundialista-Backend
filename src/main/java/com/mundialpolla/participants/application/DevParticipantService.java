package com.mundialpolla.participants.application;

import com.mundialpolla.participants.api.CreateDevParticipantRequest;
import com.mundialpolla.participants.api.ParticipantDevResponse;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.participants.infrastructure.persistence.ParticipantRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DevParticipantService {

    private final ParticipantRepository participantRepository;

    public DevParticipantService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public ParticipantDevResponse create(CreateDevParticipantRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        Participant participant = participantRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> participantRepository.save(new Participant(
                        "dev:" + normalizedEmail,
                        normalizedEmail,
                        request.displayName(),
                        null
                )));
        return toResponse(participant);
    }

    private ParticipantDevResponse toResponse(Participant participant) {
        return new ParticipantDevResponse(
                participant.getId(),
                participant.getEmail(),
                participant.getDisplayName(),
                participant.isActive()
        );
    }
}
