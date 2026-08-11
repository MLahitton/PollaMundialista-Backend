CREATE TABLE matches (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    stage_id UUID NOT NULL,
    group_id UUID,
    sportmonks_id BIGINT NOT NULL,
    home_team_id UUID NOT NULL,
    away_team_id UUID NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    prediction_closes_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL,
    home_score INTEGER,
    away_score INTEGER,
    home_penalty_score INTEGER,
    away_penalty_score INTEGER,
    qualified_team_id UUID,
    result_confirmed_at TIMESTAMPTZ,
    scored_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_matches_tournament
        FOREIGN KEY (tournament_id)
        REFERENCES tournaments (id),

    CONSTRAINT fk_matches_stage
        FOREIGN KEY (stage_id)
        REFERENCES stages (id),

    CONSTRAINT fk_matches_group
        FOREIGN KEY (group_id)
        REFERENCES tournament_groups (id),

    CONSTRAINT fk_matches_home_team
        FOREIGN KEY (home_team_id)
        REFERENCES teams (id),

    CONSTRAINT fk_matches_away_team
        FOREIGN KEY (away_team_id)
        REFERENCES teams (id),

    CONSTRAINT fk_matches_qualified_team
        FOREIGN KEY (qualified_team_id)
        REFERENCES teams (id),

    CONSTRAINT uq_matches_sportmonks_id
        UNIQUE (sportmonks_id),

    CONSTRAINT ck_matches_different_teams
        CHECK (home_team_id <> away_team_id),

    CONSTRAINT ck_matches_prediction_close
        CHECK (prediction_closes_at <= starts_at),

    CONSTRAINT ck_matches_home_score
        CHECK (home_score IS NULL OR home_score >= 0),

    CONSTRAINT ck_matches_away_score
        CHECK (away_score IS NULL OR away_score >= 0),

    CONSTRAINT ck_matches_home_penalty_score
        CHECK (
            home_penalty_score IS NULL
            OR home_penalty_score >= 0
        ),

    CONSTRAINT ck_matches_away_penalty_score
        CHECK (
            away_penalty_score IS NULL
            OR away_penalty_score >= 0
        ),

    CONSTRAINT ck_matches_penalty_pair
        CHECK (
            (
                home_penalty_score IS NULL
                AND away_penalty_score IS NULL
            )
            OR
            (
                home_penalty_score IS NOT NULL
                AND away_penalty_score IS NOT NULL
            )
        ),

    CONSTRAINT ck_matches_qualified_team
        CHECK (
            qualified_team_id IS NULL
            OR qualified_team_id = home_team_id
            OR qualified_team_id = away_team_id
        )
);

CREATE INDEX ix_matches_tournament_id
    ON matches (tournament_id);

CREATE INDEX ix_matches_stage_id
    ON matches (stage_id);

CREATE INDEX ix_matches_group_id
    ON matches (group_id);

CREATE INDEX ix_matches_home_team_id
    ON matches (home_team_id);

CREATE INDEX ix_matches_away_team_id
    ON matches (away_team_id);

CREATE INDEX ix_matches_starts_at
    ON matches (starts_at);

CREATE INDEX ix_matches_prediction_closes_at
    ON matches (prediction_closes_at);

CREATE INDEX ix_matches_status
    ON matches (status);
