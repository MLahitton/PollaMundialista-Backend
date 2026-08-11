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

    public MatchQueryService(MatchRepository matchRepository, ApplicationClock applicationClock) {
        this.matchRepository = matchRepository;
        this.applicationClock = applicationClock;
    }

    public MatchResponse findById(UUID id) {
        return matchRepository.findById(id)
                .map(this::toResponse)
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
        return map(matchRepository.findByTournamentIdAndStartsAtAfterOrderByStartsAtAsc(
                tournamentId,
                applicationClock.now()
        ));
    }

    private List<MatchResponse> map(List<Match> matches) {
        return matches.stream()
                .map(this::toResponse)
                .toList();
    }

    private MatchResponse toResponse(Match match) {
        Instant now = applicationClock.now();
        boolean hideResult = now.isBefore(match.getStartsAt());

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
                match.getStatus(),
                hideResult ? null : match.getHomeScore(),
                hideResult ? null : match.getAwayScore(),
                hideResult ? null : match.getHomePenaltyScore(),
                hideResult ? null : match.getAwayPenaltyScore(),
                hideResult || match.getQualifiedTeam() == null ? null : match.getQualifiedTeam().getId(),
                hideResult ? null : match.getResultConfirmedAt(),
                hideResult ? null : match.getScoredAt()
        );
    }
}
