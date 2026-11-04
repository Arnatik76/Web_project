-- Demo chapters with abstract placeholder pages (static/media/demo), so the reader can be tried
-- without real content. Idempotent. Disable in production by setting
-- FLYWAY_LOCATIONS=classpath:db/migration
INSERT INTO chapter (manga_id, number, title, published_on)
SELECT m.id, n, 'Демо-глава ' || n, CURRENT_DATE - ((3 - n) * 7 + (m.id % 5)::int)
FROM manga m
CROSS JOIN generate_series(1, 3) AS n
ON CONFLICT (manga_id, number) DO NOTHING;

-- Manga: 12 black-and-white pages per chapter. Manhwa/manhua: 8 tall color strips.
-- Each chapter starts at a different image so chapters do not look identical.
INSERT INTO page (chapter_id, idx, path, width, height)
SELECT c.id,
       p.idx,
       CASE WHEN m.type = 'MANGA'
            THEN '/media/demo/manga/' || lpad((((p.idx - 1) + (c.number::int - 1) * 4) % 12 + 1)::text, 2, '0') || '.webp'
            ELSE '/media/demo/manhwa/' || lpad((((p.idx - 1) + (c.number::int - 1) * 3) % 8 + 1)::text, 2, '0') || '.webp'
       END,
       CASE WHEN m.type = 'MANGA' THEN 1000 ELSE 800 END,
       CASE WHEN m.type = 'MANGA' THEN 1500 ELSE 2000 END
FROM chapter c
JOIN manga m ON m.id = c.manga_id
CROSS JOIN LATERAL generate_series(1, CASE WHEN m.type = 'MANGA' THEN 12 ELSE 8 END) AS p(idx)
WHERE c.title LIKE 'Демо-глава %'
ON CONFLICT (chapter_id, idx) DO NOTHING;
