package org.example.mangaverse.models;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;

public record Manga(
        @Id Long id,
        String title,
        String author,
        String genre,
        MangaType type,
        String description,
        BigDecimal rating,
        String imageUrl,
        @ReadOnlyProperty Instant createdAt
) {
}
