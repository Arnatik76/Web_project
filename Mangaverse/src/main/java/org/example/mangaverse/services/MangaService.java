package org.example.mangaverse.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.models.Slugs;
import org.example.mangaverse.repositories.MangaRepository;
import org.springframework.stereotype.Service;

@Service
public class MangaService {

    private final MangaRepository repository;

    public MangaService(MangaRepository repository) {
        this.repository = repository;
    }

    public Optional<Manga> findById(long id) {
        return repository.findById(id);
    }

    public Optional<Manga> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }

    public List<Manga> popular() {
        return repository.findAllByPopularity();
    }

    public List<Manga> byGenre(String genre) {
        return repository.findByGenre(genre);
    }

    public List<Manga> byType(MangaType type) {
        return repository.findByType(type.name());
    }

    public List<String> genres() {
        return repository.findGenres();
    }

    /** Case-insensitive substring search over title, author and genre. */
    public List<Manga> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return repository.search("%" + escapeLike(query.trim()) + "%");
    }

    /** Catalog listing: every filter is optional. The dataset is small, so filtering and sorting happen in memory. */
    public List<Manga> catalog(String query, String genre, MangaType type, CatalogSort sort) {
        List<Manga> base = (query != null && !query.isBlank()) ? search(query) : popular();
        return base.stream()
                .filter(m -> genre == null || m.genres().contains(genre))
                .filter(m -> type == null || type == m.type())
                .sorted(sort.comparator())
                .toList();
    }

    /** Titles similar to the given one: same primary genre first, then same format, best rated first. */
    public List<Manga> similar(Manga manga, int limit) {
        var sameGenre = byGenre(manga.genre()).stream();
        var sameType = byType(manga.type()).stream();
        return Stream.concat(sameGenre, sameType)
                .filter(m -> !m.id().equals(manga.id()))
                .distinct()
                .limit(limit)
                .toList();
    }

    /** Saves a new title, deriving a unique slug from its title: "berserk", then "berserk-2", ... */
    public Manga add(Manga manga) {
        String base = Slugs.of(manga.title());
        String slug = base;
        for (int n = 2; repository.existsBySlug(slug); n++) {
            slug = base + "-" + n;
        }
        return repository.save(manga.withSlug(slug));
    }

    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
