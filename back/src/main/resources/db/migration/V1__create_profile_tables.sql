CREATE TABLE profile (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    "name"          VARCHAR,
    surname         VARCHAR,
    birthdate       DATE,
    about           TEXT,
    gender          VARCHAR,
    photo           VARCHAR,
    status          VARCHAR NOT NULL DEFAULT 'INACTIVE',
    role            VARCHAR NOT NULL DEFAULT 'USER',
    created_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INT NOT NULL DEFAULT 0
);

CREATE TABLE profile_like (
    a_profile       BIGINT NOT NULL REFERENCES profile (id),
    b_profile       BIGINT NOT NULL REFERENCES profile (id),
    liked_a         BOOLEAN,
    liked_b         BOOLEAN,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT profile_like_pk PRIMARY KEY (a_profile, b_profile),
    CONSTRAINT profile_like_chk CHECK (a_profile < b_profile),
    FOREIGN KEY (a_profile) REFERENCES profile(id) ON DELETE CASCADE,
    FOREIGN KEY (b_profile) REFERENCES profile(id) ON DELETE CASCADE
);

CREATE INDEX idx_profile_like_a ON profile_like(a_profile);
CREATE INDEX idx_profile_like_b ON profile_like(b_profile);