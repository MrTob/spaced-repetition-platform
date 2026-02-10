CREATE TABLE cards
(
    id              UUID PRIMARY KEY,
    front           TEXT NOT NULL,
    back            TEXT NOT NULL,

    learning_step INT NOT NULL DEFAULT 0,

    easiness_factor DOUBLE PRECISION,
    interval_days   INT,
    repetitions     INT,

    stability       DOUBLE PRECISION,
    difficulty      DOUBLE PRECISION,

    next_review     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT now()
);