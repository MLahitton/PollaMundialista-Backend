package com.mundialpolla.predictions.domain;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.stages.domain.StageType;
import com.mundialpolla.teams.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "predicted_home_score", nullable = false)
    private int predictedHomeScore;

    @Column(name = "predicted_away_score", nullable = false)
    private int predictedAwayScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_qualified_team_id")
    private Team predictedQualifiedTeam;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "locked_at")
    private Instant lockedAt;

    protected Prediction() {
    }

    public Prediction(
            Participant participant,
            Match match,
            int predictedHomeScore,
            int predictedAwayScore,
            Team predictedQualifiedTeam,
            Instant submittedAt
    ) {
        this.participant = requireNonNull(participant, "participant");
        this.match = requireNonNull(match, "match");
        this.predictedHomeScore = validateScore(predictedHomeScore, "predictedHomeScore");
        this.predictedAwayScore = validateScore(predictedAwayScore, "predictedAwayScore");
        this.predictedQualifiedTeam = predictedQualifiedTeam;
        this.submittedAt = requireNonNull(submittedAt, "submittedAt");
        this.updatedAt = submittedAt;
        validatePredictedQualifiedTeam();
    }

    public void updatePrediction(
            int predictedHomeScore,
            int predictedAwayScore,
            Team predictedQualifiedTeam,
            Instant updatedAt
    ) {
        this.predictedHomeScore = validateScore(predictedHomeScore, "predictedHomeScore");
        this.predictedAwayScore = validateScore(predictedAwayScore, "predictedAwayScore");
        this.predictedQualifiedTeam = predictedQualifiedTeam;
        this.updatedAt = requireNonNull(updatedAt, "updatedAt");
        validatePredictedQualifiedTeam();
    }

    public void lock(Instant lockedAt) {
        requireNonNull(lockedAt, "lockedAt");
        if (this.lockedAt == null) {
            this.lockedAt = lockedAt;
        }
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now = Instant.now();
        if (submittedAt == null) {
            submittedAt = now;
        }
        if (updatedAt == null) {
            updatedAt = submittedAt;
        }
        validateState();
    }

    public UUID getId() {
        return id;
    }

    public Participant getParticipant() {
        return participant;
    }

    public Match getMatch() {
        return match;
    }

    public int getPredictedHomeScore() {
        return predictedHomeScore;
    }

    public int getPredictedAwayScore() {
        return predictedAwayScore;
    }

    public Team getPredictedQualifiedTeam() {
        return predictedQualifiedTeam;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    private void validateState() {
        participant = requireNonNull(participant, "participant");
        match = requireNonNull(match, "match");
        predictedHomeScore = validateScore(predictedHomeScore, "predictedHomeScore");
        predictedAwayScore = validateScore(predictedAwayScore, "predictedAwayScore");
        submittedAt = requireNonNull(submittedAt, "submittedAt");
        updatedAt = requireNonNull(updatedAt, "updatedAt");
        validatePredictedQualifiedTeam();
    }

    private void validatePredictedQualifiedTeam() {
        if (predictedQualifiedTeam == null) {
            return;
        }
        if (match.getStage().getType() == StageType.GROUP_STAGE) {
            throw new IllegalArgumentException("predictedQualifiedTeam is not allowed for group-stage matches");
        }
        if (!isSameTeam(predictedQualifiedTeam, match.getHomeTeam())
                && !isSameTeam(predictedQualifiedTeam, match.getAwayTeam())) {
            throw new IllegalArgumentException("predictedQualifiedTeam must be homeTeam or awayTeam");
        }
    }

    private static int validateScore(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static boolean isSameTeam(Team firstTeam, Team secondTeam) {
        return firstTeam == secondTeam
                || firstTeam != null
                && secondTeam != null
                && Objects.equals(firstTeam.getId(), secondTeam.getId());
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }
}
