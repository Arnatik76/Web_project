package org.example.mangaverse.controllers;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;

public record NewMangaRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 200) String author,
        @NotBlank @Size(max = 50) String genre,
        @NotNull MangaType type,
        @NotBlank @Size(max = 5000) String description,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating,
        // Only site-relative paths or https URLs: no javascript: or data: URLs.
        @NotBlank @Size(max = 500) @Pattern(regexp = "^(/|https://)[^\\s\"'<>]+$") String imageUrl
) {
    Manga toManga() {
        return new Manga(null, title.trim(), null, author.trim(), genre.trim(), type, description.trim(), rating, imageUrl, null);
    }
}
