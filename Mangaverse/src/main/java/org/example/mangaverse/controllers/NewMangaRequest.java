package org.example.mangaverse.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
        // Optional extra genres; the primary genre is always first.
        @Size(max = 9) List<@NotBlank @Size(max = 50) String> genres,
        @NotNull MangaType type,
        @NotBlank @Size(max = 5000) String description,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating,
        // Only site-relative paths or https URLs: no javascript: or data: URLs.
        @NotBlank @Size(max = 500) @Pattern(regexp = "^(/|https://)[^\\s\"'<>]+$") String imageUrl
) {
    Manga toManga() {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        all.add(genre.trim());
        if (genres != null) {
            genres.forEach(g -> all.add(g.trim()));
        }
        return new Manga(null, title.trim(), null, author.trim(), genre.trim(), new ArrayList<>(all),
                type, description.trim(), rating, imageUrl, null);
    }
}
