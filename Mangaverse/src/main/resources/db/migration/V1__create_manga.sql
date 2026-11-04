CREATE TABLE manga (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(200) NOT NULL UNIQUE,
    author      VARCHAR(200) NOT NULL,
    genre       VARCHAR(50)  NOT NULL,
    type        VARCHAR(10)  NOT NULL DEFAULT 'MANGA'
                CHECK (type IN ('MANGA', 'MANHWA', 'MANHUA')),
    description TEXT         NOT NULL,
    rating      NUMERIC(3, 2) NOT NULL CHECK (rating BETWEEN 0 AND 5),
    image_url   VARCHAR(500) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX manga_genre_idx  ON manga (genre);
CREATE INDEX manga_rating_idx ON manga (rating DESC);
