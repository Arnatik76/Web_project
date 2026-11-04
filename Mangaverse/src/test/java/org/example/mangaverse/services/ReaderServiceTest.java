package org.example.mangaverse.services;

import static org.example.mangaverse.TestData.chapter;
import static org.example.mangaverse.TestData.manga;
import static org.example.mangaverse.TestData.page;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.example.mangaverse.models.Chapter;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.repositories.ChapterRepository;
import org.example.mangaverse.repositories.PageRepository;
import org.junit.jupiter.api.Test;

class ReaderServiceTest {

    private final MangaService mangaService = mock(MangaService.class);
    private final ChapterRepository chapters = mock(ChapterRepository.class);
    private final PageRepository pages = mock(PageRepository.class);
    private final ReaderService service = new ReaderService(mangaService, chapters, pages);

    private final Manga solo = manga(2, "Solo Leveling", "Фэнтези", MangaType.MANHWA, "3.7");
    private final Chapter c1 = chapter(10, 2, "1");
    private final Chapter c15 = chapter(11, 2, "1.5");
    private final Chapter c2 = chapter(12, 2, "2");

    @Test
    void opensChapterWithNeighbours() {
        when(mangaService.findBySlug("solo-leveling")).thenReturn(Optional.of(solo));
        when(chapters.findByMangaId(2)).thenReturn(List.of(c1, c15, c2));
        when(pages.findByChapterId(11)).thenReturn(List.of(page(11, 1)));

        var view = service.open("solo-leveling", "1.5").orElseThrow();
        assertEquals(c15, view.chapter());
        assertEquals(c1, view.previous());
        assertEquals(c2, view.next());
        assertEquals(1, view.pages().size());
    }

    @Test
    void firstAndLastChapterHaveNoNeighbourOnThatSide() {
        when(mangaService.findBySlug("solo-leveling")).thenReturn(Optional.of(solo));
        when(chapters.findByMangaId(2)).thenReturn(List.of(c1, c2));

        assertNull(service.open("solo-leveling", "1").orElseThrow().previous());
        assertNull(service.open("solo-leveling", "2").orElseThrow().next());
    }

    @Test
    void matchesNumericallyNotTextually() {
        when(mangaService.findBySlug("solo-leveling")).thenReturn(Optional.of(solo));
        when(chapters.findByMangaId(2)).thenReturn(List.of(c1, c2));

        assertTrue(service.open("solo-leveling", "2.0").isPresent());
    }

    @Test
    void rejectsMalformedChapterNumbersWithoutQuerying() {
        for (String bad : new String[] {"abc", "-1", "1e3", "1.25", "", "123456"}) {
            assertTrue(service.open("solo-leveling", bad).isEmpty(), bad);
        }
        verify(mangaService, never()).findBySlug(anyString());
    }

    @Test
    void chapterNumberLabels() {
        assertEquals("1", c1.numberLabel());
        assertEquals("1.5", c15.numberLabel());
        assertEquals("10", chapter(1, 1, "10.0").numberLabel());
        assertEquals("Глава 1.5", c15.label());
    }
}
