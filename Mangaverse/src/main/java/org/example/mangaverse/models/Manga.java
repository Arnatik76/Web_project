package org.example.mangaverse.models;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;

public record Manga(
        @Id Long id,
        String title,
        String slug,
        String author,
        String genre,
        MangaType type,
        String description,
        BigDecimal rating,
        String imageUrl,
        @ReadOnlyProperty Instant createdAt
) {
    public Manga withSlug(String newSlug) {
        return new Manga(id, title, newSlug, author, genre, type, description, rating, imageUrl, createdAt);
    }
}
