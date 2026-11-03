package org.example.mangaverse.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;

/** {@code genre} is the primary genre and always equals {@code genres.get(0)}. */
public record Manga(
        @Id Long id,
        String title,
        String slug,
        String author,
        String genre,
        List<String> genres,
        MangaType type,
        String description,
        BigDecimal rating,
        String imageUrl,
        @ReadOnlyProperty Instant createdAt
) {
    public Manga withSlug(String newSlug) {
        return new Manga(id, title, newSlug, author, genre, genres, type, description, rating, imageUrl, createdAt);
    }
}
