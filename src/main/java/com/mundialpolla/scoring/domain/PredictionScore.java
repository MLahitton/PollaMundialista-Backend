package com.mundialpolla.scoring.domain;

import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.participants.domain.Participant;
import com.mundialpolla.predictions.domain.Prediction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prediction_scores")
public class PredictionScore {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "base_points", nullable = false)
    private int basePoints;

    @Column(name = "qualified_team_bonus", nullable = false)
    private int qualifiedTeamBonus;

    @Column(name = "total_points", nullable = false)
    private int totalPoints;

    @Column(name = "exact_score", nullable = false)
    private boolean exactScore;

    @Column(name = "correct_outcome", nullable = false)
    private boolean correctOutcome;

    @Column(name = "correct_qualified_team", nullable = false)
    private boolean correctQualifiedTeam;

    @Column(name = "scored_at", nullable = false)
    private Instant scoredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PredictionScore() {
    }

    public PredictionScore(
            Prediction prediction,
            Participant participant,
            Match match,
            int basePoints,
            int qualifiedTeamBonus,
            boolean exactScore,
            boolean correctOutcome,
            boolean correctQualifiedTeam,
            Instant scoredAt
    ) {
        this.prediction = requireNonNull(prediction, "prediction");
        this.participant = requireNonNull(participant, "participant");
        this.match = requireNonNull(match, "match");
        applyScore(basePoints, qualifiedTeamBonus, exactScore, correctOutcome, correctQualifiedTeam, scoredAt);
        this.createdAt = scoredAt;
    }

    public void updateScore(
            int basePoints,
            int qualifiedTeamBonus,
            boolean exactScore,
            boolean correctOutcome,
            boolean correctQualifiedTeam,
            Instant scoredAt
    ) {
        applyScore(basePoints, qualifiedTeamBonus, exactScore, correctOutcome, correctQualifiedTeam, scoredAt);
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = scoredAt;
        }
        if (updatedAt == null) {
            updatedAt = scoredAt;
        }
        validateState();
    }

    public UUID getId() {
        return id;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public Participant getParticipant() {
        return participant;
    }

    public Match getMatch() {
        return match;
    }

    public int getBasePoints() {
        return basePoints;
    }

    public int getQualifiedTeamBonus() {
        return qualifiedTeamBonus;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public boolean isExactScore() {
        return exactScore;
    }

    public boolean isCorrectOutcome() {
        return correctOutcome;
    }

    public boolean isCorrectQualifiedTeam() {
        return correctQualifiedTeam;
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

    private void applyScore(
            int basePoints,
            int qualifiedTeamBonus,
            boolean exactScore,
            boolean correctOutcome,
            boolean correctQualifiedTeam,
            Instant scoredAt
    ) {
        this.basePoints = validateBasePoints(basePoints);
        this.qualifiedTeamBonus = validateQualifiedTeamBonus(qualifiedTeamBonus);
        this.totalPoints = basePoints + qualifiedTeamBonus;
        this.exactScore = exactScore;
        this.correctOutcome = correctOutcome;
        this.correctQualifiedTeam = correctQualifiedTeam;
        this.scoredAt = requireNonNull(scoredAt, "scoredAt");
        this.updatedAt = scoredAt;
        validatePointFlags();
    }

    private void validateState() {
        prediction = requireNonNull(prediction, "prediction");
        participant = requireNonNull(participant, "participant");
        match = requireNonNull(match, "match");
        basePoints = validateBasePoints(basePoints);
        qualifiedTeamBonus = validateQualifiedTeamBonus(qualifiedTeamBonus);
        totalPoints = basePoints + qualifiedTeamBonus;
        scoredAt = requireNonNull(scoredAt, "scoredAt");
        createdAt = requireNonNull(createdAt, "createdAt");
        updatedAt = requireNonNull(updatedAt, "updatedAt");
        validatePointFlags();
    }

    private void validatePointFlags() {
        if (exactScore && basePoints != 5) {
            throw new IllegalArgumentException("exactScore requires 5 base points");
        }
        if (correctOutcome && basePoints == 0) {
            throw new IllegalArgumentException("correctOutcome requires base points");
        }
    }

    private static int validateBasePoints(int value) {
        if (value != 0 && value != 3 && value != 5) {
            throw new IllegalArgumentException("basePoints must be 0, 3, or 5");
        }
        return value;
    }

    private static int validateQualifiedTeamBonus(int value) {
        if (value != 0 && value != 1) {
            throw new IllegalArgumentException("qualifiedTeamBonus must be 0 or 1");
        }
        return value;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }
}
