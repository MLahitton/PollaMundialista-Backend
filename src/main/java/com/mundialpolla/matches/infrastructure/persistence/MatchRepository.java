package com.mundialpolla.matches.infrastructure.persistence;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.domain.MatchStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    Optional<Match> findByExternalId(Long externalId);

    boolean existsByExternalId(Long externalId);

    List<Match> findByTournamentIdOrderByStartsAtAsc(UUID tournamentId);

    List<Match> findByStageIdOrderByStartsAtAsc(UUID stageId);

    List<Match> findByGroupIdOrderByStartsAtAsc(UUID groupId);

    List<Match> findByHomeTeamIdOrAwayTeamIdOrderByStartsAtAsc(UUID homeTeamId, UUID awayTeamId);

    List<Match> findByStatusOrderByStartsAtAsc(MatchStatus status);

    List<Match> findByStartsAtBetweenOrderByStartsAtAsc(Instant from, Instant to);

    List<Match> findByTournamentIdAndStartsAtAfterOrderByStartsAtAsc(UUID tournamentId, Instant now);

    @Query("""
            select match
            from Match match
            where match.resultConfirmedAt is not null
              and match.resultConfirmedAt <= :now
              and match.startsAt <= :now
              and match.scoredAt is null
            order by match.startsAt asc
            """)
    List<Match> findAutomaticScoringCandidates(@Param("now") Instant now);
}
