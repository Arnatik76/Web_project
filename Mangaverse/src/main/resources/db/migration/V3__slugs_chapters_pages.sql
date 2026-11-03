-- Human-readable URLs: /title/solo-leveling instead of /title/10
ALTER TABLE manga ADD COLUMN slug VARCHAR(220);

UPDATE manga SET slug = trim(BOTH '-' FROM regexp_replace(lower(title), '[^a-z0-9]+', '-', 'g'));
UPDATE manga SET slug = 'omniscient-readers-viewpoint' WHERE title = 'Omniscient Reader''s Viewpoint';
UPDATE manga SET slug = 'bastard' WHERE title = 'Сволочь';
UPDATE manga SET slug = 'sweet-home' WHERE title = 'Милый дом';
UPDATE manga SET slug = 'title-' || id WHERE slug IS NULL OR slug = '' OR slug ~ '^[0-9]+$';

ALTER TABLE manga ALTER COLUMN slug SET NOT NULL;
ALTER TABLE manga ADD CONSTRAINT manga_slug_key UNIQUE (slug);
-- Lowercase words joined by hyphens, never all digits (numeric paths are legacy ids).
ALTER TABLE manga ADD CONSTRAINT manga_slug_format
    CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$' AND slug !~ '^[0-9]+$');

CREATE TABLE chapter (
    id           BIGSERIAL     PRIMARY KEY,
    manga_id     BIGINT        NOT NULL REFERENCES manga (id) ON DELETE CASCADE,
    number       NUMERIC(6, 1) NOT NULL CHECK (number >= 0),
    title        VARCHAR(200),
    published_on DATE          NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (manga_id, number)
);

-- width/height let the reader reserve space before an image loads, so the layout never jumps.
CREATE TABLE page (
    id         BIGSERIAL    PRIMARY KEY,
    chapter_id BIGINT       NOT NULL REFERENCES chapter (id) ON DELETE CASCADE,
    idx        INT          NOT NULL CHECK (idx >= 1),
    path       VARCHAR(500) NOT NULL,
    width      INT          NOT NULL CHECK (width > 0),
    height     INT          NOT NULL CHECK (height > 0),
    UNIQUE (chapter_id, idx)
);
