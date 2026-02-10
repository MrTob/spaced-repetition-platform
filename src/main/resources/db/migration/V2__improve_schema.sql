-- Use TIMESTAMPTZ for timezone awareness (matches Java Instant)
ALTER TABLE cards ALTER COLUMN next_review TYPE TIMESTAMPTZ;
ALTER TABLE cards ALTER COLUMN created_at TYPE TIMESTAMPTZ;

-- SM-2 default values (easinessFactor starts at 2.5 per the algorithm)
ALTER TABLE cards ALTER COLUMN easiness_factor SET DEFAULT 2.5;
ALTER TABLE cards ALTER COLUMN interval_days SET DEFAULT 0;
ALTER TABLE cards ALTER COLUMN repetitions SET DEFAULT 0;

-- FSRS default values
ALTER TABLE cards ALTER COLUMN stability SET DEFAULT 0;
ALTER TABLE cards ALTER COLUMN difficulty SET DEFAULT 0;

-- Add NOT NULL constraints (safe because defaults fill existing NULLs)
UPDATE cards SET easiness_factor = 2.5 WHERE easiness_factor IS NULL;
UPDATE cards SET interval_days = 0 WHERE interval_days IS NULL;
UPDATE cards SET repetitions = 0 WHERE repetitions IS NULL;
UPDATE cards SET stability = 0 WHERE stability IS NULL;
UPDATE cards SET difficulty = 0 WHERE difficulty IS NULL;
UPDATE cards SET next_review = now() WHERE next_review IS NULL;

ALTER TABLE cards ALTER COLUMN easiness_factor SET NOT NULL;
ALTER TABLE cards ALTER COLUMN interval_days SET NOT NULL;
ALTER TABLE cards ALTER COLUMN repetitions SET NOT NULL;
ALTER TABLE cards ALTER COLUMN stability SET NOT NULL;
ALTER TABLE cards ALTER COLUMN difficulty SET NOT NULL;
ALTER TABLE cards ALTER COLUMN next_review SET NOT NULL;

-- Index for the due-cards query (WHERE next_review < now())
CREATE INDEX idx_cards_next_review ON cards (next_review);
