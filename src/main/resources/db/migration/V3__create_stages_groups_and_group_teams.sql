CREATE TABLE stages (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    sportmonks_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    stage_type VARCHAR(50) NOT NULL,
    order_number INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_stages_tournament
        FOREIGN KEY (tournament_id)
        REFERENCES tournaments (id),

    CONSTRAINT uq_stages_sportmonks_id
        UNIQUE (sportmonks_id),

    CONSTRAINT uq_stages_tournament_order
        UNIQUE (tournament_id, order_number),

    CONSTRAINT ck_stages_order_number
        CHECK (order_number > 0)
);

CREATE INDEX ix_stages_tournament_id
    ON stages (tournament_id);

CREATE INDEX ix_stages_is_active
    ON stages (is_active);

CREATE TABLE tournament_groups (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    stage_id UUID NOT NULL,
    sportmonks_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    order_number INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_tournament_groups_tournament
        FOREIGN KEY (tournament_id)
        REFERENCES tournaments (id),

    CONSTRAINT fk_tournament_groups_stage
        FOREIGN KEY (stage_id)
        REFERENCES stages (id),

    CONSTRAINT uq_tournament_groups_sportmonks_id
        UNIQUE (sportmonks_id),

    CONSTRAINT uq_tournament_groups_tournament_code
        UNIQUE (tournament_id, code),

    CONSTRAINT uq_tournament_groups_stage_order
        UNIQUE (stage_id, order_number),

    CONSTRAINT ck_tournament_groups_order_number
        CHECK (order_number > 0)
);

CREATE INDEX ix_tournament_groups_tournament_id
    ON tournament_groups (tournament_id);

CREATE INDEX ix_tournament_groups_stage_id
    ON tournament_groups (stage_id);

CREATE TABLE group_teams (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    team_id UUID NOT NULL,
    order_number INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_group_teams_group
        FOREIGN KEY (group_id)
        REFERENCES tournament_groups (id),

    CONSTRAINT fk_group_teams_team
        FOREIGN KEY (team_id)
        REFERENCES teams (id),

    CONSTRAINT uq_group_teams_group_team
        UNIQUE (group_id, team_id),

    CONSTRAINT uq_group_teams_group_order
        UNIQUE (group_id, order_number),

    CONSTRAINT ck_group_teams_order_number
        CHECK (order_number > 0)
);

CREATE INDEX ix_group_teams_group_id
    ON group_teams (group_id);

CREATE INDEX ix_group_teams_team_id
    ON group_teams (team_id);
