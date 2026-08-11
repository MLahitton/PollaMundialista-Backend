CREATE TABLE participants (
    id UUID PRIMARY KEY,
    google_subject VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    profile_image_url VARCHAR(1000),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    last_login_at TIMESTAMPTZ
);

ALTER TABLE participants
    ADD CONSTRAINT uq_participants_google_subject UNIQUE (google_subject);

CREATE UNIQUE INDEX uq_participants_email_lower
    ON participants (LOWER(email));

CREATE INDEX ix_participants_is_active
    ON participants (is_active);
