package com.mundialpolla.teams.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Team() {
    }

    public Team(
            Long externalId,
            String name,
            String shortName,
            String code,
            String countryCode,
            String logoUrl
    ) {
        this.externalId = externalId;
        this.name = normalizeRequired(name, "name");
        this.shortName = normalizeOptional(shortName);
        this.code = normalizeCode(code);
        this.countryCode = normalizeCode(countryCode);
        this.logoUrl = normalizeOptional(logoUrl);
    }

    public void updateDetails(String name, String shortName, String code, String countryCode, String logoUrl) {
        this.name = normalizeRequired(name, "name");
        this.shortName = normalizeOptional(shortName);
        this.code = normalizeCode(code);
        this.countryCode = normalizeCode(countryCode);
        this.logoUrl = normalizeOptional(logoUrl);
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

    public Long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getCode() {
        return code;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLogoUrl() {
        return logoUrl;
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
        shortName = normalizeOptional(shortName);
        code = normalizeCode(code);
        countryCode = normalizeCode(countryCode);
        logoUrl = normalizeOptional(logoUrl);
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }

        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private static String normalizeCode(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }
}
