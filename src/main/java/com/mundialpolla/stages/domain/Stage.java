package com.mundialpolla.stages.domain;

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
import java.util.UUID;

@Entity
@Table(name = "stages")
public class Stage {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", nullable = false, length = 50)
    private StageType type;

    @Column(name = "order_number", nullable = false)
    private int orderNumber;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Stage() {
    }

    public Stage(Tournament tournament, Long externalId, String name, StageType type, int orderNumber) {
        this.tournament = requireNonNull(tournament, "tournament");
        this.externalId = requireNonNull(externalId, "externalId");
        this.name = normalizeRequired(name, "name");
        this.type = requireNonNull(type, "type");
        this.orderNumber = validateOrderNumber(orderNumber);
    }

    public void updateDetails(String name, StageType type, int orderNumber) {
        this.name = normalizeRequired(name, "name");
        this.type = requireNonNull(type, "type");
        this.orderNumber = validateOrderNumber(orderNumber);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
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

    public Long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public StageType getType() {
        return type;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void normalizeFields() {
        tournament = requireNonNull(tournament, "tournament");
        externalId = requireNonNull(externalId, "externalId");
        name = normalizeRequired(name, "name");
        type = requireNonNull(type, "type");
        orderNumber = validateOrderNumber(orderNumber);
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }

        return value.trim();
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
