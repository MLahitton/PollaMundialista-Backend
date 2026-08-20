package com.mundialpolla.matches.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.scoring.domain.PredictionScore;
import com.mundialpolla.stages.domain.Stage;
import com.mundialpolla.stages.domain.StageType;
import com.mundialpolla.teams.domain.Team;
import com.mundialpolla.tournaments.domain.Tournament;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/**
 * Cubre la recuperacion de pronosticos que quedaron sin puntuar.
 *
 * <p>Corre contra la base de datos configurada (igual que el resto de la
 * suite) dentro de una transaccion que hace rollback: no deja datos.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AutomaticScoringCandidatesTest {

    /** externalId altos para no chocar con el dataset real del Mundial. */
    private static final AtomicLong EXTERNAL_ID = new AtomicLong(900_000_000L);

    private static final Instant MATCH_STARTS_AT = Instant.parse("2026-06-11T19:00:00Z");
    private static final Instant NOW = Instant.parse("2026-06-12T19:00:00Z");

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tournament tournament;
    private Stage stage;
    private Team homeTeam;
    private Team awayTeam;

    @BeforeEach
    void setUp() {
        tournament = entityManager.persist(new Tournament(
                nextExternalId(),
                "Test Cup",
                "2026",
                LocalDate.of(2026, 6, 11),
                LocalDate.of(2026, 7, 19)
        ));
        stage = entityManager.persist(new Stage(
                tournament,
                nextExternalId(),
                "Group Stage",
                StageType.GROUP_STAGE,
                1
        ));
        homeTeam = entityManager.persist(newTeam("Home Test"));
        awayTeam = entityManager.persist(newTeam("Away Test"));
    }

    @Test
    void matchWithoutScoredAtIsCandidate() {
        Match match = persistFinishedMatch();

        assertThat(candidateIds()).contains(match.getId());
    }

    @Test
    void scoredMatchWithUnscoredPredictionIsCandidate() {
        Match match = persistFinishedMatch();
        persistPrediction(match, persistParticipant());
        match.markScored(NOW);
        entityManager.persistAndFlush(match);

        // Ya tiene scoredAt, pero su pronostico sigue sin PredictionScore.
        assertThat(match.getScoredAt()).isNotNull();
        assertThat(candidateIds()).contains(match.getId());
    }

    @Test
    void scoredMatchWithEveryPredictionScoredIsNotCandidate() {
        Match match = persistFinishedMatch();
        Participant participant = persistParticipant();
        Prediction prediction = persistPrediction(match, participant);
        persistScore(prediction, participant, match);
        match.markScored(NOW);
        entityManager.persistAndFlush(match);

        assertThat(candidateIds()).doesNotContain(match.getId());
    }

    @Test
    void scoredMatchIsCandidateWhileAnyPredictionIsMissingItsScore() {
        Match match = persistFinishedMatch();
        Participant scored = persistParticipant();
        persistScore(persistPrediction(match, scored), scored, match);
        // Un segundo pronostico, este sin puntuar.
        persistPrediction(match, persistParticipant());
        match.markScored(NOW);
        entityManager.persistAndFlush(match);

        assertThat(candidateIds()).contains(match.getId());
    }

    @Test
    void aPredictionCannotHaveTwoScores() {
        Match match = persistFinishedMatch();
        Participant participant = persistParticipant();
        Prediction prediction = persistPrediction(match, participant);
        persistScore(prediction, participant, match);

        assertThatThrownBy(() -> persistScore(prediction, participant, match))
                .isInstanceOf(Exception.class);
    }

    private List<UUID> candidateIds() {
        entityManager.flush();
        return matchRepository.findAutomaticScoringCandidates(NOW).stream()
                .map(Match::getId)
                .toList();
    }

    private Match persistFinishedMatch() {
        Match match = new Match(
                tournament,
                stage,
                null,
                nextExternalId(),
                homeTeam,
                awayTeam,
                MATCH_STARTS_AT,
                MATCH_STARTS_AT.minusSeconds(900)
        );
        match.confirmResult(2, 0, null, null, null, MATCH_STARTS_AT.plusSeconds(7200));
        return entityManager.persistAndFlush(match);
    }

    private Prediction persistPrediction(Match match, Participant participant) {
        return entityManager.persistAndFlush(new Prediction(
                participant,
                match,
                2,
                1,
                null,
                MATCH_STARTS_AT.minusSeconds(3600)
        ));
    }

    private PredictionScore persistScore(Prediction prediction, Participant participant, Match match) {
        return entityManager.persistAndFlush(new PredictionScore(
                prediction,
                participant,
                match,
                3,
                0,
                false,
                true,
                false,
                NOW
        ));
    }

    private Participant persistParticipant() {
        String unique = UUID.randomUUID().toString();
        return entityManager.persistAndFlush(new Participant(
                "google-test-" + unique,
                "test-" + unique + "@example.test",
                "Participante de prueba",
                null
        ));
    }

    private Team newTeam(String name) {
        return new Team(nextExternalId(), name, name, null, null, null);
    }

    private static Long nextExternalId() {
        return EXTERNAL_ID.incrementAndGet();
    }
}
