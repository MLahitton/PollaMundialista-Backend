package com.mundialpolla.tournaments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "season", nullable = false, length = 50)
    private String season;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Tournament() {
    }

    public Tournament(Long externalId, String name, String season, LocalDate startDate, LocalDate endDate) {
        this.externalId = externalId;
        this.name = normalizeRequired(name, "name");
        this.season = normalizeRequired(season, "season");
        this.startDate = startDate;
        this.endDate = endDate;
        validateDateRange();
    }

    public void updateDetails(String name, String season, LocalDate startDate, LocalDate endDate) {
        this.name = normalizeRequired(name, "name");
        this.season = normalizeRequired(season, "season");
        this.startDate = startDate;
        this.endDate = endDate;
        validateDateRange();
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
        validateDateRange();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        normalizeFields();
        validateDateRange();
    }

    public UUID getId() {
        return id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getSeason() {
        return season;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
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
        name = normalizeRequired(name, "name");
        season = normalizeRequired(season, "season");
    }

    private void validateDateRange() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }

        return value.trim();
    }
}
