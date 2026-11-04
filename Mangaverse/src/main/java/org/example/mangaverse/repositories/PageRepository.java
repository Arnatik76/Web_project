package org.example.mangaverse.repositories;

import java.util.List;

import org.example.mangaverse.models.ChapterPage;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface PageRepository extends ListCrudRepository<ChapterPage, Long> {

    @Query("SELECT * FROM page WHERE chapter_id = :chapterId ORDER BY idx")
    List<ChapterPage> findByChapterId(long chapterId);
}
