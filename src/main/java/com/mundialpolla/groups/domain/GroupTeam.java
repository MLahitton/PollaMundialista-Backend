package com.mundialpolla.groups.domain;

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
import java.util.UUID;

@Entity
@Table(name = "group_teams")
public class GroupTeam {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TournamentGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "order_number", nullable = false)
    private int orderNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected GroupTeam() {
    }

    public GroupTeam(TournamentGroup group, Team team, int orderNumber) {
        this.group = requireNonNull(group, "group");
        this.team = requireNonNull(team, "team");
        this.orderNumber = validateOrderNumber(orderNumber);
    }

    public void updateOrderNumber(int orderNumber) {
        this.orderNumber = validateOrderNumber(orderNumber);
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        createdAt = Instant.now();
        normalizeFields();
    }

    public UUID getId() {
        return id;
    }

    public TournamentGroup getGroup() {
        return group;
    }

    public Team getTeam() {
        return team;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void normalizeFields() {
        group = requireNonNull(group, "group");
        team = requireNonNull(team, "team");
        orderNumber = validateOrderNumber(orderNumber);
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
