package org.example.mangaverse.services;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.repositories.MangaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MangaService {

    private final MangaRepository mangaRepository;

    public MangaService(MangaRepository mangaRepository) {
        this.mangaRepository = mangaRepository;
    }

    public Manga getMangaById(Long id) {
        return mangaRepository.getMangaById(id);
    }

    public List<Manga> getAllManga() {
        return mangaRepository.findAll();
    }

    public void addManga(Manga manga) {
        String title = manga.getTitle();
        String author = manga.getAuthor();
        String genre = manga.getGenre();
        String description = manga.getDescription();
        String imageUrl = manga.getImageUrl();

        mangaRepository.insertManga(title, author, genre, description, imageUrl);
    }

}
