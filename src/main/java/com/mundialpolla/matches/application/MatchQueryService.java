package com.mundialpolla.matches.application;

import com.mundialpolla.matches.api.MatchResponse;
import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.domain.MatchStatus;
import com.mundialpolla.matches.infrastructure.persistence.MatchRepository;
import com.mundialpolla.shared.time.ApplicationClock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class MatchQueryService {

    private final MatchRepository matchRepository;
    private final ApplicationClock applicationClock;
    private final MatchViewStateResolver matchViewStateResolver;

    public MatchQueryService(
            MatchRepository matchRepository,
            ApplicationClock applicationClock,
            MatchViewStateResolver matchViewStateResolver
    ) {
        this.matchRepository = matchRepository;
        this.applicationClock = applicationClock;
        this.matchViewStateResolver = matchViewStateResolver;
    }

    public MatchResponse findById(UUID id) {
        Instant now = applicationClock.now();
        return matchRepository.findById(id)
                .map(match -> toResponse(match, now))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
    }

    public List<MatchResponse> findByTournamentId(UUID tournamentId) {
        return map(matchRepository.findByTournamentIdOrderByStartsAtAsc(tournamentId));
    }

    public List<MatchResponse> findByStageId(UUID stageId) {
        return map(matchRepository.findByStageIdOrderByStartsAtAsc(stageId));
    }

    public List<MatchResponse> findByGroupId(UUID groupId) {
        return map(matchRepository.findByGroupIdOrderByStartsAtAsc(groupId));
    }

    public List<MatchResponse> findByTeamId(UUID teamId) {
        return map(matchRepository.findByHomeTeamIdOrAwayTeamIdOrderByStartsAtAsc(teamId, teamId));
    }

    public List<MatchResponse> findByStatus(MatchStatus status) {
        return map(matchRepository.findByStatusOrderByStartsAtAsc(status));
    }

    public List<MatchResponse> findBetween(Instant from, Instant to) {
        return map(matchRepository.findByStartsAtBetweenOrderByStartsAtAsc(from, to));
    }

    public List<MatchResponse> findUpcoming(UUID tournamentId) {
        Instant now = applicationClock.now();
        return map(matchRepository.findByTournamentIdAndStartsAtAfterOrderByStartsAtAsc(
                tournamentId,
                now
        ), now);
    }

    private List<MatchResponse> map(List<Match> matches) {
        Instant now = applicationClock.now();
        return map(matches, now);
    }

    private List<MatchResponse> map(List<Match> matches, Instant now) {
        return matches.stream()
                .map(match -> toResponse(match, now))
                .toList();
    }

    private MatchResponse toResponse(Match match, Instant now) {
        MatchViewState viewState = matchViewStateResolver.resolve(match, now);
        boolean showResult = viewState.resultVisible();
        Instant visibleResultConfirmedAt = showResult
                && match.getResultConfirmedAt() != null
                && !match.getResultConfirmedAt().isAfter(now)
                ? match.getResultConfirmedAt()
                : null;
        Instant visibleScoredAt = showResult
                && match.getScoredAt() != null
                && !match.getScoredAt().isAfter(now)
                ? match.getScoredAt()
                : null;

        return new MatchResponse(
                match.getId(),
                match.getTournament().getId(),
                match.getStage().getId(),
                match.getGroup() == null ? null : match.getGroup().getId(),
                match.getExternalId(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getCode(),
                match.getHomeTeam().getLogoUrl(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getCode(),
                match.getAwayTeam().getLogoUrl(),
                match.getStartsAt(),
                match.getPredictionClosesAt(),
                viewState.status(),
                viewState.predictionsOpen(),
                viewState.predictionsClosed(),
                viewState.resultVisible(),
                showResult ? match.getHomeScore() : null,
                showResult ? match.getAwayScore() : null,
                showResult ? match.getHomePenaltyScore() : null,
                showResult ? match.getAwayPenaltyScore() : null,
                showResult && match.getQualifiedTeam() != null ? match.getQualifiedTeam().getId() : null,
                visibleResultConfirmedAt,
                visibleScoredAt
        );
    }
}
