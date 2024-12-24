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

    public Manga getMangaById(long id) {
        return mangaRepository.GetMangaById(id);
    }

    public List<Manga> getAllManga() {
        return mangaRepository.findAll();
    }

    public Manga addManga(Manga manga) {
        return mangaRepository.save(manga);
    }

}
