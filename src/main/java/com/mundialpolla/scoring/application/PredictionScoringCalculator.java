package com.mundialpolla.scoring.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.predictions.domain.Prediction;
import com.mundialpolla.stages.domain.StageType;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PredictionScoringCalculator {

    public PredictionScoreCalculation calculate(Prediction prediction) {
        Match match = prediction.getMatch();
        if (match.getHomeScore() == null || match.getAwayScore() == null) {
            throw new IllegalArgumentException("Match does not have a confirmed score");
        }

        boolean exactScore = prediction.getPredictedHomeScore() == match.getHomeScore()
                && prediction.getPredictedAwayScore() == match.getAwayScore();
        boolean correctOutcome = exactScore || predictedOutcome(prediction) == actualOutcome(match);
        int basePoints = exactScore ? 5 : correctOutcome ? 3 : 0;
        boolean correctQualifiedTeam = hasPenaltyQualifiedTeamBonus(match)
                && prediction.getPredictedQualifiedTeam() != null
                && Objects.equals(prediction.getPredictedQualifiedTeam().getId(), match.getQualifiedTeam().getId());
        int qualifiedTeamBonus = correctQualifiedTeam ? 1 : 0;

        return new PredictionScoreCalculation(
                basePoints,
                qualifiedTeamBonus,
                basePoints + qualifiedTeamBonus,
                exactScore,
                correctOutcome,
                correctQualifiedTeam
        );
    }

    private boolean hasPenaltyQualifiedTeamBonus(Match match) {
        return match.getStage().getType() != StageType.GROUP_STAGE
                && match.getHomePenaltyScore() != null
                && match.getAwayPenaltyScore() != null
                && match.getQualifiedTeam() != null;
    }

    private MatchOutcome predictedOutcome(Prediction prediction) {
        return outcome(prediction.getPredictedHomeScore(), prediction.getPredictedAwayScore());
    }

    private MatchOutcome actualOutcome(Match match) {
        return outcome(match.getHomeScore(), match.getAwayScore());
    }

    private MatchOutcome outcome(int homeScore, int awayScore) {
        if (homeScore > awayScore) {
            return MatchOutcome.HOME_WIN;
        }
        if (homeScore < awayScore) {
            return MatchOutcome.AWAY_WIN;
        }
        return MatchOutcome.DRAW;
    }

    private enum MatchOutcome {
        HOME_WIN,
        DRAW,
        AWAY_WIN
    }
}
