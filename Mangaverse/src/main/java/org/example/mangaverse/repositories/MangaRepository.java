package org.example.mangaverse.repositories;

import org.example.mangaverse.models.Manga;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends CrudRepository<Manga, Long> {

    @Query("SELECT * FROM public.manga WHERE id = :id")
    Manga GetMangaById(long id);
}
