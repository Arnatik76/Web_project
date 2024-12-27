package org.example.mangaverse.controllers;

import java.util.List;
import java.util.logging.Logger;

import org.example.mangaverse.exceptions.MangaNotFoundException;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.services.MangaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manga")
public class MangaApiController {

    private static final Logger logger = Logger.getLogger(MangaApiController.class.getName());
    private final MangaService mangaService;

    public MangaApiController(MangaService mangaService) {
        this.mangaService = mangaService;
    }

    @GetMapping("/{mangaId}")
    public ResponseEntity<Manga> getManga(@PathVariable Long mangaId) {
        logger.info("GET /api/manga/" + mangaId);
        Manga manga = mangaService.getMangaById(mangaId);
        if (manga == null) {
            logger.info("Manga not found with id: " + mangaId);
            throw new MangaNotFoundException();
        } else {
            logger.info("Manga found: " + manga);
        }

        return ResponseEntity.ok(manga);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Manga>> getAllManga() {
        logger.info("GET /api/manga/all");
        List<Manga> mangaList = mangaService.getAllManga();
        if (mangaList.isEmpty()) {
            logger.info("No manga found");
        } else {
            logger.info("Manga found: " + mangaList.size());
        }

        return ResponseEntity.ok(mangaList);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Manga>> getPopularManga() {
        logger.info("GET /api/manga/popular");
        List<Manga> mangaList = mangaService.getPopularManga();
        if (mangaList.isEmpty()) {
            logger.info("No manga found");
        } else {
            logger.info("Manga found: " + mangaList.size());
        }

        return ResponseEntity.ok(mangaList);
    }

    @GetMapping("/genre")
    public ResponseEntity<List<Manga>> getMangaByGenre(@RequestParam String genre) {
        logger.info("GET /api/manga/genre?genre=" + genre);
        List<Manga> mangaList = mangaService.getMangaByGenre(genre);
        if (mangaList.isEmpty()) {
            logger.info("No manga found");
        } else {
            logger.info("Manga found: " + mangaList.size());
        }

        return ResponseEntity.ok(mangaList);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Manga>> searchManga(@RequestParam String query) {
        logger.info("GET /api/manga/search?query=" + query);
        List<Manga> mangaList = mangaService.searchManga(query);
        if (mangaList.isEmpty()) {
            logger.info("No manga found");
        } else {
            logger.info("Manga found: " + mangaList.size());
        }

        return ResponseEntity.ok(mangaList);
    }

    @PostMapping("/new")
    public ResponseEntity<Manga> addManga(@RequestBody Manga manga) {
        logger.info("POST /api/manga/new");
        mangaService.addManga(manga);
        return ResponseEntity.ok(manga);
    }

}
