package org.example.mangaverse.repositories;

import java.util.List;
import java.util.Optional;

import org.example.mangaverse.models.Manga;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface MangaRepository extends ListCrudRepository<Manga, Long> {

    @Query("SELECT * FROM manga WHERE slug = :slug")
    Optional<Manga> findBySlug(String slug);

    @Query("SELECT count(*) > 0 FROM manga WHERE slug = :slug")
    boolean existsBySlug(String slug);

    @Query("SELECT * FROM manga ORDER BY rating DESC, title")
    List<Manga> findAllByPopularity();

    @Query("SELECT * FROM manga WHERE genre = :genre ORDER BY rating DESC, title")
    List<Manga> findByGenre(String genre);

    @Query("SELECT * FROM manga WHERE type = :type ORDER BY rating DESC, title")
    List<Manga> findByType(String type);

    /** {@code pattern} must already be a LIKE pattern with wildcards escaped, see {@code MangaService#search}. */
    @Query("""
            SELECT * FROM manga
            WHERE title ILIKE :pattern OR author ILIKE :pattern OR genre ILIKE :pattern
            ORDER BY rating DESC, title
            """)
    List<Manga> search(String pattern);

    /** Genres ordered by how many titles they have, most common first. */
    @Query("SELECT genre FROM manga GROUP BY genre ORDER BY count(*) DESC, genre")
    List<String> findGenres();
}
