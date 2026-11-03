CREATE TABLE app_user (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(254) NOT NULL UNIQUE CHECK (email = lower(email)),
    name          VARCHAR(60)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- One shelf status per title. updated_at drives last-write-wins sync between devices.
-- status NULL is a tombstone: "removed from the library", so a removal also syncs.
CREATE TABLE library_entry (
    user_id    BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    manga_id   BIGINT      NOT NULL REFERENCES manga (id) ON DELETE CASCADE,
    status     VARCHAR(12) CHECK (status IN ('READING', 'PLANNED', 'COMPLETED', 'ON_HOLD', 'DROPPED')),
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, manga_id)
);

-- Where the user stopped in each title.
CREATE TABLE reading_progress (
    user_id    BIGINT        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    manga_id   BIGINT        NOT NULL REFERENCES manga (id) ON DELETE CASCADE,
    chapter    NUMERIC(6, 1) NOT NULL CHECK (chapter >= 0),
    page       INT           NOT NULL CHECK (page >= 1),
    total      INT           NOT NULL CHECK (total >= page),
    finished   BOOLEAN       NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ   NOT NULL,
    PRIMARY KEY (user_id, manga_id)
);
CREATE INDEX reading_progress_recent_idx ON reading_progress (user_id, updated_at DESC);
