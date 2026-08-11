package com.mundialpolla.matches.domain;

import com.mundialpolla.groups.domain.TournamentGroup;
import com.mundialpolla.stages.domain.Stage;
import com.mundialpolla.teams.domain.Team;
import com.mundialpolla.tournaments.domain.Tournament;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private TournamentGroup group;

    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "prediction_closes_at", nullable = false)
    private Instant predictionClosesAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "home_penalty_score")
    private Integer homePenaltyScore;

    @Column(name = "away_penalty_score")
    private Integer awayPenaltyScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualified_team_id")
    private Team qualifiedTeam;

    @Column(name = "result_confirmed_at")
    private Instant resultConfirmedAt;

    @Column(name = "scored_at")
    private Instant scoredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Match() {
    }

    public Match(
            Tournament tournament,
            Stage stage,
            TournamentGroup group,
            Long externalId,
            Team homeTeam,
            Team awayTeam,
            Instant startsAt,
            Instant predictionClosesAt
    ) {
        this.tournament = requireNonNull(tournament, "tournament");
        this.stage = requireNonNull(stage, "stage");
        this.group = group;
        this.externalId = requireNonNull(externalId, "externalId");
        this.homeTeam = requireNonNull(homeTeam, "homeTeam");
        this.awayTeam = requireNonNull(awayTeam, "awayTeam");
        this.startsAt = requireNonNull(startsAt, "startsAt");
        this.predictionClosesAt = requireNonNull(predictionClosesAt, "predictionClosesAt");
        validateTeams();
        validatePredictionClose();
    }

    public void updateSchedule(
            Stage stage,
            TournamentGroup group,
            Team homeTeam,
            Team awayTeam,
            Instant startsAt,
            Instant predictionClosesAt
    ) {
        if (status == MatchStatus.FINISHED || status == MatchStatus.SCORED || status == MatchStatus.CANCELLED) {
            throw new IllegalStateException("match cannot be rescheduled in current status");
        }

        this.stage = requireNonNull(stage, "stage");
        this.group = group;
        this.homeTeam = requireNonNull(homeTeam, "homeTeam");
        this.awayTeam = requireNonNull(awayTeam, "awayTeam");
        this.startsAt = requireNonNull(startsAt, "startsAt");
        this.predictionClosesAt = requireNonNull(predictionClosesAt, "predictionClosesAt");
        validateTeams();
        validatePredictionClose();
        validateQualifiedTeam();
    }

    public void markOpenForPredictions() {
        requireStatus(MatchStatus.SCHEDULED, MatchStatus.POSTPONED);
        status = MatchStatus.OPEN_FOR_PREDICTIONS;
    }

    public void markPredictionClosed() {
        requireStatus(MatchStatus.OPEN_FOR_PREDICTIONS, MatchStatus.SCHEDULED);
        status = MatchStatus.PREDICTION_CLOSED;
    }

    public void markInProgress() {
        requireStatus(MatchStatus.PREDICTION_CLOSED, MatchStatus.OPEN_FOR_PREDICTIONS, MatchStatus.SCHEDULED);
        status = MatchStatus.IN_PROGRESS;
    }

    public void markPostponed() {
        rejectStatus(MatchStatus.FINISHED, MatchStatus.SCORED);
        status = MatchStatus.POSTPONED;
    }

    public void markCancelled() {
        rejectStatus(MatchStatus.FINISHED, MatchStatus.SCORED);
        status = MatchStatus.CANCELLED;
    }

    public void markPendingReview() {
        requireStatus(MatchStatus.IN_PROGRESS, MatchStatus.FINISHED, MatchStatus.PENDING_REVIEW);
        status = MatchStatus.PENDING_REVIEW;
    }

    public void confirmResult(
            int homeScore,
            int awayScore,
            Integer homePenaltyScore,
            Integer awayPenaltyScore,
            Team qualifiedTeam,
            Instant confirmedAt
    ) {
        validateNonNegative(homeScore, "homeScore");
        validateNonNegative(awayScore, "awayScore");
        validateNullableNonNegative(homePenaltyScore, "homePenaltyScore");
        validateNullableNonNegative(awayPenaltyScore, "awayPenaltyScore");
        validatePenaltyPair(homePenaltyScore, awayPenaltyScore);
        validatePenaltyWinner(homePenaltyScore, awayPenaltyScore);

        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homePenaltyScore = homePenaltyScore;
        this.awayPenaltyScore = awayPenaltyScore;
        this.qualifiedTeam = qualifiedTeam;
        this.resultConfirmedAt = requireNonNull(confirmedAt, "confirmedAt");
        validateQualifiedTeam();
        validateQualifiedTeamForPenalties();
        status = MatchStatus.FINISHED;
    }

    public void markScored(Instant scoredAt) {
        requireStatus(MatchStatus.FINISHED, MatchStatus.PENDING_REVIEW);
        this.scoredAt = requireNonNull(scoredAt, "scoredAt");
        status = MatchStatus.SCORED;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        validateState();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        validateState();
    }

    public UUID getId() {
        return id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public Stage getStage() {
        return stage;
    }

    public TournamentGroup getGroup() {
        return group;
    }

    public Long getExternalId() {
        return externalId;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getPredictionClosesAt() {
        return predictionClosesAt;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public Integer getHomePenaltyScore() {
        return homePenaltyScore;
    }

    public Integer getAwayPenaltyScore() {
        return awayPenaltyScore;
    }

    public Team getQualifiedTeam() {
        return qualifiedTeam;
    }

    public Instant getResultConfirmedAt() {
        return resultConfirmedAt;
    }

    public Instant getScoredAt() {
        return scoredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void validateState() {
        tournament = requireNonNull(tournament, "tournament");
        stage = requireNonNull(stage, "stage");
        externalId = requireNonNull(externalId, "externalId");
        homeTeam = requireNonNull(homeTeam, "homeTeam");
        awayTeam = requireNonNull(awayTeam, "awayTeam");
        startsAt = requireNonNull(startsAt, "startsAt");
        predictionClosesAt = requireNonNull(predictionClosesAt, "predictionClosesAt");
        status = requireNonNull(status, "status");
        validateTeams();
        validatePredictionClose();
        validateNullableNonNegative(homeScore, "homeScore");
        validateNullableNonNegative(awayScore, "awayScore");
        validateNullableNonNegative(homePenaltyScore, "homePenaltyScore");
        validateNullableNonNegative(awayPenaltyScore, "awayPenaltyScore");
        validatePenaltyPair(homePenaltyScore, awayPenaltyScore);
        validatePenaltyWinner(homePenaltyScore, awayPenaltyScore);
        validateQualifiedTeam();
        validateQualifiedTeamForPenalties();
    }

    private void validateTeams() {
        if (isSameTeam(homeTeam, awayTeam)) {
            throw new IllegalArgumentException("homeTeam and awayTeam must be different");
        }
    }

    private void validatePredictionClose() {
        if (predictionClosesAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("predictionClosesAt must be before or equal to startsAt");
        }
    }

    private void validateQualifiedTeam() {
        if (qualifiedTeam != null && !isSameTeam(qualifiedTeam, homeTeam) && !isSameTeam(qualifiedTeam, awayTeam)) {
            throw new IllegalArgumentException("qualifiedTeam must be homeTeam or awayTeam");
        }
    }

    private void validateQualifiedTeamForPenalties() {
        if ((homePenaltyScore != null || awayPenaltyScore != null) && qualifiedTeam == null) {
            throw new IllegalArgumentException("qualifiedTeam is required when penalties exist");
        }
    }

    private static void validatePenaltyPair(Integer homePenaltyScore, Integer awayPenaltyScore) {
        if ((homePenaltyScore == null) != (awayPenaltyScore == null)) {
            throw new IllegalArgumentException("penalty scores must be both null or both present");
        }
    }

    private static void validatePenaltyWinner(Integer homePenaltyScore, Integer awayPenaltyScore) {
        if (homePenaltyScore != null && homePenaltyScore.equals(awayPenaltyScore)) {
            throw new IllegalArgumentException("penalty scores must not be equal");
        }
    }

    private static void validateNullableNonNegative(Integer value, String fieldName) {
        if (value != null) {
            validateNonNegative(value, fieldName);
        }
    }

    private static void validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
    }

    private void requireStatus(MatchStatus... allowedStatuses) {
        for (MatchStatus allowedStatus : allowedStatuses) {
            if (status == allowedStatus) {
                return;
            }
        }

        throw new IllegalStateException("invalid match status transition");
    }

    private void rejectStatus(MatchStatus... rejectedStatuses) {
        for (MatchStatus rejectedStatus : rejectedStatuses) {
            if (status == rejectedStatus) {
                throw new IllegalStateException("invalid match status transition");
            }
        }
    }

    private static boolean isSameTeam(Team firstTeam, Team secondTeam) {
        if (firstTeam == secondTeam) {
            return true;
        }

        return firstTeam != null
                && secondTeam != null
                && firstTeam.getId() != null
                && Objects.equals(firstTeam.getId(), secondTeam.getId());
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }

        return value;
    }
}
