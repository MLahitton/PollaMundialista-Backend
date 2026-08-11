package com.mundialpolla.groups.domain;

import com.mundialpolla.stages.domain.Stage;
import com.mundialpolla.tournaments.domain.Tournament;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "tournament_groups")
public class TournamentGroup {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "order_number", nullable = false)
    private int orderNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TournamentGroup() {
    }

    public TournamentGroup(Tournament tournament, Stage stage, Long externalId, String name, String code, int orderNumber) {
        this.tournament = requireNonNull(tournament, "tournament");
        this.stage = requireNonNull(stage, "stage");
        this.externalId = requireNonNull(externalId, "externalId");
        this.name = normalizeRequired(name, "name");
        this.code = normalizeCode(code);
        this.orderNumber = validateOrderNumber(orderNumber);
    }

    public void updateDetails(String name, String code, int orderNumber) {
        this.name = normalizeRequired(name, "name");
        this.code = normalizeCode(code);
        this.orderNumber = validateOrderNumber(orderNumber);
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        normalizeFields();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        normalizeFields();
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

    public Long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void normalizeFields() {
        tournament = requireNonNull(tournament, "tournament");
        stage = requireNonNull(stage, "stage");
        externalId = requireNonNull(externalId, "externalId");
        name = normalizeRequired(name, "name");
        code = normalizeCode(code);
        orderNumber = validateOrderNumber(orderNumber);
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }

        return value.trim();
    }

    private static String normalizeCode(String value) {
        return normalizeRequired(value, "code").toUpperCase(Locale.ROOT);
    }

    private static int validateOrderNumber(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("orderNumber must be greater than zero");
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
