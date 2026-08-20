package com.mundialpolla.matches.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.domain.MatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias — Resolvedor de Estados de Vista (MatchViewStateResolver)")
class MatchViewStateResolverTest {

    private MatchViewStateResolver resolver;

    @Mock
    private Match match;

    private final Instant now = Instant.parse("2026-06-11T18:40:00Z");

    @BeforeEach
    void setUp() {
        resolver = new MatchViewStateResolver();
    }

    @Test
    @DisplayName("Caso 1: Antes del cierre -> Estado OPEN_FOR_PREDICTIONS con predicciones abiertas")
    void shouldResolveOpenForPredictionsWhenBeforeCloseTime() {
        when(match.getStatus()).thenReturn(MatchStatus.SCHEDULED);
        when(match.getPredictionClosesAt()).thenReturn(Instant.parse("2026-06-11T18:45:00Z"));

        MatchViewState state = resolver.resolve(match, now);

        assertEquals(MatchViewStatus.OPEN_FOR_PREDICTIONS, state.status());
        assertTrue(state.predictionsOpen(), "predictionsOpen debe ser true");
        assertFalse(state.predictionsClosed(), "predictionsClosed debe ser false");
        assertFalse(state.resultVisible(), "resultVisible debe ser false");
    }

    @Test
    @DisplayName("Caso 2: Entre hora de cierre y hora de inicio -> Estado PREDICTION_CLOSED")
    void shouldResolvePredictionClosedWhenBetweenCloseAndStart() {
        when(match.getStatus()).thenReturn(MatchStatus.SCHEDULED);
        when(match.getPredictionClosesAt()).thenReturn(Instant.parse("2026-06-11T18:30:00Z"));
        when(match.getStartsAt()).thenReturn(Instant.parse("2026-06-11T19:00:00Z"));

        MatchViewState state = resolver.resolve(match, now);

        assertEquals(MatchViewStatus.PREDICTION_CLOSED, state.status());
        assertFalse(state.predictionsOpen());
        assertTrue(state.predictionsClosed());
        assertFalse(state.resultVisible());
    }

    @Test
    @DisplayName("Caso 3: Partido Cancelado -> Estado CANCELLED inmediato")
    void shouldResolveCancelledWhenMatchStatusIsCancelled() {
        when(match.getStatus()).thenReturn(MatchStatus.CANCELLED);

        MatchViewState state = resolver.resolve(match, now);

        assertEquals(MatchViewStatus.CANCELLED, state.status());
        assertFalse(state.predictionsOpen());
        assertTrue(state.predictionsClosed());
    }

    @Test
    @DisplayName("Caso 4: Partido Postergado -> Estado POSTPONED inmediato")
    void shouldResolvePostponedWhenMatchStatusIsPostponed() {
        when(match.getStatus()).thenReturn(MatchStatus.POSTPONED);

        MatchViewState state = resolver.resolve(match, now);

        assertEquals(MatchViewStatus.POSTPONED, state.status());
        assertFalse(state.predictionsOpen());
        assertTrue(state.predictionsClosed());
    }
}
