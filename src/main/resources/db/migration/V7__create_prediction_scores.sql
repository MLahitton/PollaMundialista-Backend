CREATE TABLE prediction_scores (
    id UUID PRIMARY KEY,
    prediction_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    match_id UUID NOT NULL,
    base_points INTEGER NOT NULL,
    qualified_team_bonus INTEGER NOT NULL,
    total_points INTEGER NOT NULL,
    exact_score BOOLEAN NOT NULL,
    correct_outcome BOOLEAN NOT NULL,
    correct_qualified_team BOOLEAN NOT NULL,
    scored_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_prediction_scores_prediction
        FOREIGN KEY (prediction_id)
        REFERENCES predictions (id),

    CONSTRAINT fk_prediction_scores_participant
        FOREIGN KEY (participant_id)
        REFERENCES participants (id),

    CONSTRAINT fk_prediction_scores_match
        FOREIGN KEY (match_id)
        REFERENCES matches (id),

    CONSTRAINT uq_prediction_scores_prediction
        UNIQUE (prediction_id),

    CONSTRAINT ck_prediction_scores_base_points
        CHECK (base_points IN (0, 3, 5)),

    CONSTRAINT ck_prediction_scores_qualified_team_bonus
        CHECK (qualified_team_bonus IN (0, 1)),

    CONSTRAINT ck_prediction_scores_total_points_non_negative
        CHECK (total_points >= 0),

    CONSTRAINT ck_prediction_scores_total_points_sum
        CHECK (total_points = base_points + qualified_team_bonus)
);

CREATE INDEX ix_prediction_scores_prediction_id
ON prediction_scores (prediction_id);

CREATE INDEX ix_prediction_scores_participant_id
ON prediction_scores (participant_id);

CREATE INDEX ix_prediction_scores_match_id
ON prediction_scores (match_id);
