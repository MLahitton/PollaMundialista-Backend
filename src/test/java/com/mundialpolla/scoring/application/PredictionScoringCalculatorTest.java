package com.mundialpolla.scoring.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.stages.domain.Stage;
import com.mundialpolla.stages.domain.StageType;
import com.mundialpolla.teams.domain.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias — Motor de Scoring (PredictionScoringCalculator)")
class PredictionScoringCalculatorTest {

    private PredictionScoringCalculator calculator;

    @Mock
    private Prediction prediction;

    @Mock
    private Match match;

    @Mock
    private Stage stage;

    @Mock
    private Team homeTeam;

    @BeforeEach
    void setUp() {
        calculator = new PredictionScoringCalculator();
        lenient().when(prediction.getMatch()).thenReturn(match);
        lenient().when(match.getStage()).thenReturn(stage);
    }

    @Test
    @DisplayName("Caso 1: Marcador Exacto en Fase de Grupos -> Otorga 5 puntos base (Total: 5 pts)")
    void shouldAward5PointsForExactScoreInGroupStage() {
        // Pronóstico: 2 - 1 | Real: 2 - 1
        when(prediction.getPredictedHomeScore()).thenReturn(2);
        when(prediction.getPredictedAwayScore()).thenReturn(1);
        when(match.getHomeScore()).thenReturn(2);
        when(match.getAwayScore()).thenReturn(1);
        when(stage.getType()).thenReturn(StageType.GROUP_STAGE);

        PredictionScoreCalculation result = calculator.calculate(prediction);

        assertAll(
                () -> assertEquals(5, result.basePoints(), "Debe otorgar 5 puntos base"),
                () -> assertEquals(0, result.qualifiedTeamBonus(), "Fase de grupos no otorga bonus de penales"),
                () -> assertEquals(5, result.totalPoints(), "Puntos totales deben ser 5"),
                () -> assertTrue(result.exactScore(), "exactScore debe ser true"),
                () -> assertTrue(result.correctOutcome(), "correctOutcome debe ser true"),
                () -> assertFalse(result.correctQualifiedTeam(), "correctQualifiedTeam debe ser false")
        );
    }

    @Test
    @DisplayName("Caso 2: Ganador Acertado con Marcador Distinto en Fase de Grupos -> Otorga 3 puntos base")
    void shouldAward3PointsForCorrectWinnerInGroupStage() {
        // Pronóstico: 3 - 0 | Real: 2 - 1 (Ambos victoria local)
        when(prediction.getPredictedHomeScore()).thenReturn(3);
        when(prediction.getPredictedAwayScore()).thenReturn(0);
        when(match.getHomeScore()).thenReturn(2);
        when(match.getAwayScore()).thenReturn(1);
        when(stage.getType()).thenReturn(StageType.GROUP_STAGE);

        PredictionScoreCalculation result = calculator.calculate(prediction);

        assertAll(
                () -> assertEquals(3, result.basePoints(), "Debe otorgar 3 puntos por acierto de ganador"),
                () -> assertEquals(0, result.qualifiedTeamBonus(), "Sin bonus"),
                () -> assertEquals(3, result.totalPoints(), "Total 3 puntos"),
                () -> assertFalse(result.exactScore(), "exactScore debe ser false"),
                () -> assertTrue(result.correctOutcome(), "correctOutcome debe ser true")
        );
    }

    @Test
    @DisplayName("Caso 3: Empate Acertado con Marcador Distinto en Fase de Grupos -> Otorga 3 puntos base")
    void shouldAward3PointsForCorrectDrawInGroupStage() {
        // Pronóstico: 1 - 1 | Real: 2 - 2 (Ambos empate)
        when(prediction.getPredictedHomeScore()).thenReturn(1);
        when(prediction.getPredictedAwayScore()).thenReturn(1);
        when(match.getHomeScore()).thenReturn(2);
        when(match.getAwayScore()).thenReturn(2);
        when(stage.getType()).thenReturn(StageType.GROUP_STAGE);

        PredictionScoreCalculation result = calculator.calculate(prediction);

        assertAll(
                () -> assertEquals(3, result.basePoints(), "Debe otorgar 3 puntos por acierto de empate"),
                () -> assertEquals(3, result.totalPoints(), "Total 3 puntos"),
                () -> assertFalse(result.exactScore(), "exactScore debe ser false"),
                () -> assertTrue(result.correctOutcome(), "correctOutcome debe ser true")
        );
    }

    @Test
    @DisplayName("Caso 4: Pronóstico Errado (Tendencia Contraria) -> Otorga 0 puntos")
    void shouldAward0PointsForWrongPrediction() {
        // Pronóstico: 1 - 0 (Victoria local) | Real: 0 - 2 (Victoria visitante)
        when(prediction.getPredictedHomeScore()).thenReturn(1);
        when(prediction.getPredictedAwayScore()).thenReturn(0);
        when(match.getHomeScore()).thenReturn(0);
        when(match.getAwayScore()).thenReturn(2);
        when(stage.getType()).thenReturn(StageType.GROUP_STAGE);

        PredictionScoreCalculation result = calculator.calculate(prediction);

        assertAll(
                () -> assertEquals(0, result.basePoints(), "Debe ser 0 puntos"),
                () -> assertEquals(0, result.totalPoints(), "Total 0 puntos"),
                () -> assertFalse(result.exactScore(), "exactScore debe ser false"),
                () -> assertFalse(result.correctOutcome(), "correctOutcome debe ser false")
        );
    }

    @Test
    @DisplayName("Caso 5: Fase Eliminatoria con Penales -> 3 Puntos por Empate + 1 Punto Bonus por Clasificado (Total: 4 pts)")
    void shouldAwardBonusPointForKnockoutQualifiedTeamInPenalties() {
        // Pronóstico: 0 - 0, Clasifica: HomeTeam | Real: 1 - 1, Penales: 5-4, Clasifica: HomeTeam
        UUID teamId = UUID.randomUUID();
        when(homeTeam.getId()).thenReturn(teamId);

        when(prediction.getPredictedHomeScore()).thenReturn(0);
        when(prediction.getPredictedAwayScore()).thenReturn(0);
        when(prediction.getPredictedQualifiedTeam()).thenReturn(homeTeam);

        when(match.getHomeScore()).thenReturn(1);
        when(match.getAwayScore()).thenReturn(1);
        when(match.getHomePenaltyScore()).thenReturn(5);
        when(match.getAwayPenaltyScore()).thenReturn(4);
        when(match.getQualifiedTeam()).thenReturn(homeTeam);

        when(stage.getType()).thenReturn(StageType.ROUND_OF_16);

        PredictionScoreCalculation result = calculator.calculate(prediction);

        assertAll(
                () -> assertEquals(3, result.basePoints(), "3 puntos base por acertar empate en los 90/120 min"),
                () -> assertEquals(1, result.qualifiedTeamBonus(), "1 punto bonus por acertar clasificado en penales"),
                () -> assertEquals(4, result.totalPoints(), "Total 4 puntos (3 base + 1 bonus)"),
                () -> assertTrue(result.correctOutcome(), "correctOutcome debe ser true"),
                () -> assertTrue(result.correctQualifiedTeam(), "correctQualifiedTeam debe ser true")
        );
    }

    @Test
    @DisplayName("Caso 6: Excepción de Validación -> Lanza IllegalArgumentException si el partido no tiene resultado confirmado")
    void shouldThrowExceptionIfMatchHasNoConfirmedScore() {
        when(match.getHomeScore()).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(prediction),
                "Debe lanzar IllegalArgumentException"
        );

        assertEquals("Match does not have a confirmed score", exception.getMessage());
    }
}
