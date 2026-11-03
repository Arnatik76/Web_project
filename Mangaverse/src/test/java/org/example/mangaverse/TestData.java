package org.example.mangaverse;

import java.math.BigDecimal;

import java.time.LocalDate;

import org.example.mangaverse.models.Chapter;
import org.example.mangaverse.models.ChapterPage;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.models.Slugs;

public final class TestData {

    private TestData() {
    }

    public static Manga manga(long id, String title, String genre, MangaType type, String rating) {
        return new Manga(id, title, Slugs.of(title), "Author", genre, type, "Description of " + title,
                new BigDecimal(rating), "/images/naruto_cover_page.jpg", null);
    }

    public static Chapter chapter(long id, long mangaId, String number) {
        return new Chapter(id, mangaId, new BigDecimal(number), "Демо-глава " + number, LocalDate.of(2026, 11, 3));
    }

    public static ChapterPage page(long chapterId, int idx) {
        return new ChapterPage((long) idx, chapterId, idx, "/media/demo/manga/0" + idx + ".webp", 1000, 1500);
    }
}
