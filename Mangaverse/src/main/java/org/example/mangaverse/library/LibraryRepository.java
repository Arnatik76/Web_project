package org.example.mangaverse.library;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Library and progress rows. Every write is a last-write-wins upsert: it only applies when it is newer
 * than what is stored, so devices can sync in any order. Timestamps travel as epoch milliseconds.
 */
@Repository
public class LibraryRepository {

    private final JdbcClient jdbc;

    public LibraryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** @return false when the title does not exist or a newer change is already stored */
    public boolean upsertStatus(long userId, String slug, LibraryStatus status, long updatedAt) {
        return jdbc.sql("""
                        INSERT INTO library_entry (user_id, manga_id, status, updated_at)
                        SELECT :user, m.id, :status, to_timestamp(:ts / 1000.0) FROM manga m WHERE m.slug = :slug
                        ON CONFLICT (user_id, manga_id) DO UPDATE
                            SET status = EXCLUDED.status, updated_at = EXCLUDED.updated_at
                            WHERE library_entry.updated_at < EXCLUDED.updated_at
                        """)
                .param("user", userId)
                .param("status", status == null ? null : status.name())
                .param("ts", updatedAt)
                .param("slug", slug)
                .update() > 0;
    }

    public boolean upsertProgress(long userId, ProgressItem item, long updatedAt) {
        return jdbc.sql("""
                        INSERT INTO reading_progress (user_id, manga_id, chapter, page, total, finished, updated_at)
                        SELECT :user, m.id, :chapter, :page, :total, :finished, to_timestamp(:ts / 1000.0)
                        FROM manga m WHERE m.slug = :slug
                        ON CONFLICT (user_id, manga_id) DO UPDATE
                            SET chapter = EXCLUDED.chapter, page = EXCLUDED.page, total = EXCLUDED.total,
                                finished = EXCLUDED.finished, updated_at = EXCLUDED.updated_at
                            WHERE reading_progress.updated_at < EXCLUDED.updated_at
                        """)
                .param("user", userId)
                .param("chapter", new BigDecimal(item.chapter()))
                .param("page", item.page())
                .param("total", item.total())
                .param("finished", item.finished())
                .param("ts", updatedAt)
                .param("slug", item.slug())
                .update() > 0;
    }

    public boolean mangaExists(String slug) {
        return jdbc.sql("SELECT count(*) FROM manga WHERE slug = :slug").param("slug", slug).query(Long.class).single() > 0;
    }

    /** Includes tombstones (status null) so other devices learn about removals. */
    public List<LibraryItem> findLibrary(long userId) {
        return jdbc.sql("""
                        SELECT m.slug, l.status, (extract(epoch FROM l.updated_at) * 1000)::bigint AS ts,
                               m.title, m.image_url
                        FROM library_entry l JOIN manga m ON m.id = l.manga_id
                        WHERE l.user_id = :user
                        ORDER BY l.updated_at DESC
                        """)
                .param("user", userId)
                .query((rs, n) -> new LibraryItem(rs.getString("slug"),
                        rs.getString("status") == null ? null : LibraryStatus.valueOf(rs.getString("status")),
                        rs.getLong("ts"), rs.getString("title"), rs.getString("image_url")))
                .list();
    }

    public List<ProgressItem> findProgress(long userId) {
        return jdbc.sql("""
                        SELECT m.slug, p.chapter, p.page, p.total, p.finished,
                               (extract(epoch FROM p.updated_at) * 1000)::bigint AS ts, m.title, m.image_url
                        FROM reading_progress p JOIN manga m ON m.id = p.manga_id
                        WHERE p.user_id = :user
                        ORDER BY p.updated_at DESC
                        """)
                .param("user", userId)
                .query((rs, n) -> new ProgressItem(rs.getString("slug"),
                        rs.getBigDecimal("chapter").stripTrailingZeros().toPlainString(),
                        rs.getInt("page"), rs.getInt("total"), rs.getBoolean("finished"), rs.getLong("ts"),
                        rs.getString("title"), rs.getString("image_url")))
                .list();
    }
}
