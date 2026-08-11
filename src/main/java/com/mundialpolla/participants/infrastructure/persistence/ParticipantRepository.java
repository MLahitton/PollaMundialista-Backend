package com.mundialpolla.participants.infrastructure.persistence;

import com.mundialpolla.participants.domain.Participant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    Optional<Participant> findByGoogleSubject(String googleSubject);

    Optional<Participant> findByEmailIgnoreCase(String email);

    boolean existsByGoogleSubject(String googleSubject);

    boolean existsByEmailIgnoreCase(String email);
}
