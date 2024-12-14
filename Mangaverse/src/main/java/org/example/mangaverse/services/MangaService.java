package org.example.mangaverse.services;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.repositories.MangaRepository;
import org.springframework.stereotype.Service;

@Service
public class MangaService {

    private final MangaRepository mangaRepository;

    public MangaService(MangaRepository mangaRepository) {
        this.mangaRepository = mangaRepository;
    }

    public Manga getMangaById(long id) {
        return mangaRepository.GetMangaById(id);
    }

}
