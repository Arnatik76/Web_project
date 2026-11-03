package org.example.mangaverse;

import java.math.BigDecimal;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;

public final class TestData {

    private TestData() {
    }

    public static Manga manga(long id, String title, String genre, MangaType type, String rating) {
        return new Manga(id, title, "Author", genre, type, "Description of " + title,
                new BigDecimal(rating), "/images/naruto_cover_page.jpg", null);
    }
}
