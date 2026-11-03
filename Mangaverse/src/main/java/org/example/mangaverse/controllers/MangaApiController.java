package org.example.mangaverse.controllers;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.example.mangaverse.exceptions.MangaNotFoundException;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.services.MangaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manga")
public class MangaApiController {

    private static final Logger log = LoggerFactory.getLogger(MangaApiController.class);

    private final MangaService mangaService;

    public MangaApiController(MangaService mangaService) {
        this.mangaService = mangaService;
    }

    @GetMapping("/{id}")
    public Manga getManga(@PathVariable long id) {
        return mangaService.findById(id).orElseThrow(() -> new MangaNotFoundException(id));
    }

    @GetMapping("/all")
    public List<Manga> getAll() {
        return mangaService.popular();
    }

    @GetMapping("/popular")
    public List<Manga> getPopular() {
        return mangaService.popular();
    }

    @GetMapping("/genre")
    public List<Manga> getByGenre(@RequestParam String genre) {
        return mangaService.byGenre(genre);
    }

    @GetMapping("/genres")
    public List<String> getGenres() {
        return mangaService.genres();
    }

    @GetMapping("/search")
    public List<Manga> search(@RequestParam(name = "query", required = false) String query,
                              @RequestParam(name = "q", required = false) String q) {
        return mangaService.search(query != null ? query : q);
    }

    /** Requires the X-Admin-Token header, see {@code AdminTokenInterceptor}. */
    @PostMapping("/new")
    public ResponseEntity<Manga> addManga(@Valid @RequestBody NewMangaRequest request) {
        Manga saved = mangaService.add(request.toManga());
        log.info("Added manga id={} title={}", saved.id(), saved.title());
        return ResponseEntity.created(URI.create("/api/manga/" + saved.id())).body(saved);
    }
}
