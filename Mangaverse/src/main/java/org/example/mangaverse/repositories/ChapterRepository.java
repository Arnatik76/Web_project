package org.example.mangaverse.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.example.mangaverse.models.Chapter;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface ChapterRepository extends ListCrudRepository<Chapter, Long> {

    @Query("SELECT * FROM chapter WHERE manga_id = :mangaId ORDER BY number")
    List<Chapter> findByMangaId(long mangaId);

    @Query("SELECT * FROM chapter WHERE manga_id = :mangaId AND number = :number")
    Optional<Chapter> findByMangaIdAndNumber(long mangaId, BigDecimal number);
}
