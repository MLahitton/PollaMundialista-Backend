package com.mundialpolla.matches.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.domain.MatchStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class MatchViewStateResolver {

    public MatchViewState resolve(Match match, Instant now) {
        if (match.getStatus() == MatchStatus.CANCELLED) {
            return new MatchViewState(MatchViewStatus.CANCELLED, false, true, false);
        }
        if (match.getStatus() == MatchStatus.POSTPONED) {
            return new MatchViewState(MatchViewStatus.POSTPONED, false, true, false);
        }
        if (now.isBefore(match.getPredictionClosesAt())) {
            return new MatchViewState(MatchViewStatus.OPEN_FOR_PREDICTIONS, true, false, false);
        }
        if (now.isBefore(match.getStartsAt())) {
            return new MatchViewState(MatchViewStatus.PREDICTION_CLOSED, false, true, false);
        }
        if (match.getResultConfirmedAt() == null || match.getResultConfirmedAt().isAfter(now)) {
            return new MatchViewState(MatchViewStatus.IN_PROGRESS, false, true, false);
        }
        if (match.getScoredAt() != null && !match.getScoredAt().isAfter(now)) {
            return new MatchViewState(MatchViewStatus.SCORED, false, true, true);
        }
        if (!match.getResultConfirmedAt().isAfter(now)) {
            return new MatchViewState(MatchViewStatus.FINISHED, false, true, true);
        }

        return new MatchViewState(MatchViewStatus.IN_PROGRESS, false, true, false);
    }
}
