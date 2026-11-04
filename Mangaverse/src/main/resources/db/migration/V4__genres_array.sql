-- A title can have several genres. The first one is the primary genre shown on cards (manga.genre).
ALTER TABLE manga ADD COLUMN genres TEXT[] NOT NULL DEFAULT '{}';
UPDATE manga SET genres = ARRAY[genre];

UPDATE manga SET genres = ARRAY['Боевик', 'Приключения']                  WHERE slug = 'naruto';
UPDATE manga SET genres = ARRAY['Фэнтези', 'Боевик', 'Приключения']       WHERE slug = 'omniscient-readers-viewpoint';
UPDATE manga SET genres = ARRAY['Спорт', 'Драма']                         WHERE slug = 'windbreaker';
UPDATE manga SET genres = ARRAY['Фэнтези', 'Приключения', 'Боевик']       WHERE slug = 'the-beginning-after-the-end';
UPDATE manga SET genres = ARRAY['Триллер', 'Ужасы', 'Драма']              WHERE slug = 'bastard';
UPDATE manga SET genres = ARRAY['Ужасы', 'Триллер']                       WHERE slug = 'sweet-home';
UPDATE manga SET genres = ARRAY['Приключения', 'Боевик']                  WHERE slug = 'steel-ball-run';
UPDATE manga SET genres = ARRAY['Комедия', 'Боевик']                      WHERE slug = 'one-punch-man';
UPDATE manga SET genres = ARRAY['Фэнтези', 'Боевик']                      WHERE slug = 'solo-leveling';
UPDATE manga SET genres = ARRAY['Драма', 'Боевик', 'Фэнтези']             WHERE slug = 'attack-on-titan';
UPDATE manga SET genres = ARRAY['Боевик', 'Комедия']                      WHERE slug = 'my-hero-academia';
UPDATE manga SET genres = ARRAY['Ужасы', 'Боевик', 'Драма']               WHERE slug = 'tokyo-ghoul';
UPDATE manga SET genres = ARRAY['Триллер', 'Драма']                       WHERE slug = 'death-note';
UPDATE manga SET genres = ARRAY['Боевик', 'Фэнтези']                      WHERE slug = 'demon-slayer';
UPDATE manga SET genres = ARRAY['Боевик', 'Фэнтези']                      WHERE slug = 'bleach';
UPDATE manga SET genres = ARRAY['Драма', 'Боевик']                        WHERE slug = 'vagabond';
UPDATE manga SET genres = ARRAY['Фэнтези', 'Боевик', 'Комедия']           WHERE slug = 'black-clover';
UPDATE manga SET genres = ARRAY['Боевик', 'Ужасы', 'Комедия']             WHERE slug = 'chainsaw-man';
UPDATE manga SET genres = ARRAY['Приключения', 'Боевик', 'Фэнтези']       WHERE slug = 'hunter-x-hunter';

UPDATE manga SET genre = genres[1];

ALTER TABLE manga ADD CONSTRAINT manga_genres_not_empty CHECK (cardinality(genres) BETWEEN 1 AND 10);
ALTER TABLE manga ADD CONSTRAINT manga_primary_genre CHECK (genres[1] = genre);

CREATE INDEX manga_genres_idx ON manga USING GIN (genres);
DROP INDEX manga_genre_idx;
