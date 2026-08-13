package com.mundialpolla.scoring.application;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.infrastructure.persistence.MatchRepository;
import com.mundialpolla.shared.time.ApplicationClock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AutomaticScoringService {

    private static final Logger log = LoggerFactory.getLogger(AutomaticScoringService.class);

    private final MatchRepository matchRepository;
    private final MatchScoringService matchScoringService;
    private final ApplicationClock applicationClock;

    public AutomaticScoringService(
            MatchRepository matchRepository,
            MatchScoringService matchScoringService,
            ApplicationClock applicationClock
    ) {
        this.matchRepository = matchRepository;
        this.matchScoringService = matchScoringService;
        this.applicationClock = applicationClock;
    }

    public AutomaticScoringRunResult runOnce() {
        Instant now = applicationClock.now();
        log.info("Automatic scoring cycle started: asOf={}", now);
        List<Match> candidates = matchRepository.findAutomaticScoringCandidates(now);
        log.info("Automatic scoring candidates found: count={}", candidates.size());

        int processed = 0;
        int failed = 0;
        for (Match match : candidates) {
            try {
                matchScoringService.scoreMatch(match.getId());
                processed++;
                log.info("Automatic scoring processed matchId={}", match.getId());
            } catch (RuntimeException exception) {
                failed++;
                log.error("Automatic scoring failed for matchId={}", match.getId(), exception);
            }
        }

        log.info(
                "Automatic scoring completed: candidates={} processed={} failed={}",
                candidates.size(),
                processed,
                failed
        );
        return new AutomaticScoringRunResult(now, candidates.size(), processed, failed);
    }
}
