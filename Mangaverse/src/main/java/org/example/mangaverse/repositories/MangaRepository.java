package org.example.mangaverse.repositories;

import org.example.mangaverse.models.Manga;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MangaRepository extends PagingAndSortingRepository<Manga, Long> {

    @Query("SELECT * FROM public.manga WHERE id = :id")
    Manga getMangaById(Long id);

    @Query("SELECT * FROM public.manga")
    List<Manga> findAll();

    @Modifying
    @Query("INSERT INTO public.manga (title, author, genre, description) VALUES (:title, :author, :genre, :description)")
    void insertManga(String title, String author, String genre, String description);
}
