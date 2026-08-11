package com.mundialpolla.participants.domain;

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
@Table(name = "participants")
public class Participant {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "google_subject", nullable = false, unique = true, length = 255)
    private String googleSubject;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected Participant() {
    }

    public Participant(String googleSubject, String email, String displayName, String profileImageUrl) {
        this.googleSubject = normalizeRequired(googleSubject);
        this.email = normalizeEmail(email);
        this.displayName = normalizeRequired(displayName);
        this.profileImageUrl = normalizeOptional(profileImageUrl);
    }

    public void updateProfile(String email, String displayName, String profileImageUrl) {
        this.email = normalizeEmail(email);
        this.displayName = normalizeRequired(displayName);
        this.profileImageUrl = normalizeOptional(profileImageUrl);
    }

    public void registerLogin() {
        this.lastLoginAt = Instant.now();
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

    public String getGoogleSubject() {
        return googleSubject;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
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

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    private void normalizeFields() {
        googleSubject = normalizeRequired(googleSubject);
        email = normalizeEmail(email);
        displayName = normalizeRequired(displayName);
        profileImageUrl = normalizeOptional(profileImageUrl);
    }

    private static String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
