CREATE TABLE tournaments (
    id UUID PRIMARY KEY,
    sportmonks_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    season VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_tournaments_sportmonks_id UNIQUE (sportmonks_id),
    CONSTRAINT ck_tournaments_date_range CHECK (start_date <= end_date)
);

CREATE INDEX ix_tournaments_is_active
    ON tournaments (is_active);

CREATE TABLE teams (
    id UUID PRIMARY KEY,
    sportmonks_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    short_name VARCHAR(100),
    code VARCHAR(10),
    country_code VARCHAR(10),
    logo_url VARCHAR(1000),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_teams_sportmonks_id UNIQUE (sportmonks_id)
);

CREATE UNIQUE INDEX uq_teams_code_upper
    ON teams (UPPER(code))
    WHERE code IS NOT NULL;

CREATE INDEX ix_teams_is_active
    ON teams (is_active);
