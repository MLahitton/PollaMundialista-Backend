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

    /**
     * Partidos que el scoring automatico debe procesar.
     *
     * <p>Marcar el partido como puntuado y puntuar sus pronosticos son hechos
     * independientes: un partido puede quedar con scoredAt pero sin ningun
     * PredictionScore si se puntuo cuando todavia no existian pronosticos.
     * Filtrar solo por {@code scoredAt is null} dejaba esos pronosticos sin
     * puntuar de forma permanente, y el ranking los ignoraba para siempre.
     *
     * <p>Por eso un partido tambien vuelve a ser candidato cuando conserva al
     * menos un pronostico sin PredictionScore. Los partidos ya puntuados por
     * completo quedan fuera, asi que no se reprocesan.
     */
    @Query("""
            select match
            from Match match
            where match.resultConfirmedAt is not null
              and match.resultConfirmedAt <= :now
              and match.startsAt <= :now
              and (
                    match.scoredAt is null
                    or exists (
                        select 1
                        from Prediction prediction
                        where prediction.match = match
                          and not exists (
                              select 1
                              from PredictionScore score
                              where score.prediction = prediction
                          )
                    )
                  )
            order by match.startsAt asc
            """)
    List<Match> findAutomaticScoringCandidates(@Param("now") Instant now);
}
