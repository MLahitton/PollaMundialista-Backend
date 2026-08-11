CREATE TABLE predictions (
    id UUID PRIMARY KEY,
    participant_id UUID NOT NULL,
    match_id UUID NOT NULL,
    predicted_home_score INTEGER NOT NULL,
    predicted_away_score INTEGER NOT NULL,
    predicted_qualified_team_id UUID,
    submitted_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    locked_at TIMESTAMPTZ,

    CONSTRAINT fk_predictions_participant
        FOREIGN KEY (participant_id)
        REFERENCES participants (id),

    CONSTRAINT fk_predictions_match
        FOREIGN KEY (match_id)
        REFERENCES matches (id),

    CONSTRAINT fk_predictions_qualified_team
        FOREIGN KEY (predicted_qualified_team_id)
        REFERENCES teams (id),

    CONSTRAINT uq_predictions_participant_match
        UNIQUE (participant_id, match_id),

    CONSTRAINT ck_predictions_home_score
        CHECK (predicted_home_score >= 0),

    CONSTRAINT ck_predictions_away_score
        CHECK (predicted_away_score >= 0)
);

CREATE INDEX ix_predictions_participant_id
ON predictions (participant_id);

CREATE INDEX ix_predictions_match_id
ON predictions (match_id);

CREATE INDEX ix_predictions_locked_at
ON predictions (locked_at);
