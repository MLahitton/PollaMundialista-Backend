ALTER TABLE tournaments
    RENAME COLUMN sportmonks_id TO external_id;

ALTER TABLE teams
    RENAME COLUMN sportmonks_id TO external_id;

ALTER TABLE stages
    RENAME COLUMN sportmonks_id TO external_id;

ALTER TABLE tournament_groups
    RENAME COLUMN sportmonks_id TO external_id;

ALTER TABLE matches
    RENAME COLUMN sportmonks_id TO external_id;

ALTER TABLE tournaments
    RENAME CONSTRAINT uq_tournaments_sportmonks_id
    TO uq_tournaments_external_id;

ALTER TABLE teams
    RENAME CONSTRAINT uq_teams_sportmonks_id
    TO uq_teams_external_id;

ALTER TABLE stages
    RENAME CONSTRAINT uq_stages_sportmonks_id
    TO uq_stages_external_id;

ALTER TABLE tournament_groups
    RENAME CONSTRAINT uq_tournament_groups_sportmonks_id
    TO uq_tournament_groups_external_id;

ALTER TABLE matches
    RENAME CONSTRAINT uq_matches_sportmonks_id
    TO uq_matches_external_id;
