package org.example.mangaverse.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.example.mangaverse.models.Chapter;
import org.example.mangaverse.models.ChapterPage;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.repositories.ChapterRepository;
import org.example.mangaverse.repositories.PageRepository;
import org.springframework.stereotype.Service;

@Service
public class ReaderService {

    /** Everything the reader page needs for one chapter. {@code previous}/{@code next} are null at the ends. */
    public record ChapterView(Manga manga, Chapter chapter, List<ChapterPage> pages,
                              Chapter previous, Chapter next, List<Chapter> chapters) {
    }

    private final MangaService mangaService;
    private final ChapterRepository chapters;
    private final PageRepository pages;

    public ReaderService(MangaService mangaService, ChapterRepository chapters, PageRepository pages) {
        this.mangaService = mangaService;
        this.chapters = chapters;
        this.pages = pages;
    }

    /** Chapters of a title in reading order. */
    public List<Chapter> chapters(Manga manga) {
        return chapters.findByMangaId(manga.id());
    }

    public Optional<ChapterView> open(String slug, String chapterNumber) {
        BigDecimal number = parseNumber(chapterNumber);
        if (number == null) {
            return Optional.empty();
        }
        return mangaService.findBySlug(slug).flatMap(manga -> {
            List<Chapter> all = chapters(manga);
            for (int i = 0; i < all.size(); i++) {
                Chapter chapter = all.get(i);
                if (chapter.number().compareTo(number) == 0) {
                    return Optional.of(new ChapterView(manga, chapter, pages.findByChapterId(chapter.id()),
                            i > 0 ? all.get(i - 1) : null,
                            i < all.size() - 1 ? all.get(i + 1) : null,
                            all));
                }
            }
            return Optional.empty();
        });
    }

    private static BigDecimal parseNumber(String value) {
        if (value == null || !value.matches("\\d{1,5}(\\.\\d)?")) {
            return null;
        }
        return new BigDecimal(value);
    }
}
