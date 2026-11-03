package org.example.mangaverse.controllers;

import static org.example.mangaverse.TestData.chapter;
import static org.example.mangaverse.TestData.manga;
import static org.example.mangaverse.TestData.page;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.example.mangaverse.models.Chapter;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.services.CatalogSort;
import org.example.mangaverse.services.MangaService;
import org.example.mangaverse.services.ReaderService;
import org.example.mangaverse.services.ReaderService.ChapterView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PageController.class)
@TestPropertySource(properties = {"spring.web.locale=ru", "spring.web.locale-resolver=fixed"})
class PageControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MangaService mangaService;

    @MockitoBean
    ReaderService readerService;

    private final Manga naruto = manga(1, "Naruto", "Боевик", MangaType.MANGA, "4.7");
    private final Manga solo = manga(2, "Solo Leveling", "Фэнтези", MangaType.MANHWA, "3.7");

    @Test
    void homeShowsFeaturedAndShelves() throws Exception {
        when(mangaService.popular()).thenReturn(List.of(naruto, solo));
        when(mangaService.genres()).thenReturn(List.of("Боевик", "Фэнтези"));
        when(mangaService.byGenre("Боевик")).thenReturn(List.of(naruto));
        when(mangaService.byGenre("Фэнтези")).thenReturn(List.of(solo));
        when(mangaService.byType(MangaType.MANHWA)).thenReturn(List.of(solo));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Выбор недели")))
                .andExpect(content().string(containsString("Продолжить чтение")))
                .andExpect(content().string(containsString("/title/solo-leveling")));
    }

    @Test
    void catalogEscapesUserContent() throws Exception {
        Manga evil = manga(3, "<img src=x onerror=alert(1)>", "Драма", MangaType.MANGA, "4.0");
        when(mangaService.catalog(eq("x"), any(), any(), eq(CatalogSort.POPULAR))).thenReturn(List.of(evil));
        when(mangaService.genres()).thenReturn(List.of("Драма"));

        mvc.perform(get("/catalog").param("q", "x"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;img src=x onerror=alert(1)&gt;")))
                .andExpect(content().string(not(containsString("<img src=x"))))
                .andExpect(content().string(containsString("1 тайтл")));
    }

    @Test
    void catalogIgnoresUnknownFilterValues() throws Exception {
        when(mangaService.catalog(any(), any(), any(), any())).thenReturn(List.of());
        when(mangaService.genres()).thenReturn(List.of());

        mvc.perform(get("/catalog").param("type", "comics").param("sort", "random"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ничего не нашлось")));
    }

    @Test
    void titlePageListsChaptersAndLinksToReader() throws Exception {
        when(mangaService.findBySlug("solo-leveling")).thenReturn(Optional.of(solo));
        when(readerService.chapters(solo)).thenReturn(List.of(chapter(10, 2, "1"), chapter(11, 2, "2.5")));
        when(mangaService.similar(eq(solo), anyInt())).thenReturn(List.of(naruto));

        mvc.perform(get("/title/solo-leveling"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Начать читать")))
                .andExpect(content().string(containsString("href=\"/read/solo-leveling/1\"")))
                .andExpect(content().string(containsString("Глава 2.5")))
                .andExpect(content().string(containsString("2 главы")))
                .andExpect(content().string(containsString("3 ноября 2026")))
                .andExpect(content().string(containsString("Похожие")));
    }

    @Test
    void titleWithoutChaptersHasDisabledReadButton() throws Exception {
        when(mangaService.findBySlug("naruto")).thenReturn(Optional.of(naruto));
        when(readerService.chapters(naruto)).thenReturn(List.of());
        when(mangaService.similar(eq(naruto), anyInt())).thenReturn(List.of());

        mvc.perform(get("/title/naruto"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-disabled=\"true\"")))
                .andExpect(content().string(containsString("Главы пока не загружены")));
    }

    @Test
    void numericTitleLinkRedirectsPermanentlyToSlug() throws Exception {
        when(mangaService.findById(2)).thenReturn(Optional.of(solo));

        mvc.perform(get("/title/2"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/title/solo-leveling"));
    }

    @Test
    void unknownTitleIs404() throws Exception {
        when(mangaService.findBySlug("nope")).thenReturn(Optional.empty());

        mvc.perform(get("/title/nope").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound());
    }

    @Test
    void readerRendersPagesWithDimensionsAndNeighbours() throws Exception {
        Chapter first = chapter(10, 2, "1");
        Chapter second = chapter(11, 2, "2");
        ChapterView view = new ChapterView(solo, first, List.of(page(10, 1), page(10, 2), page(10, 3)),
                null, second, List.of(first, second));
        when(readerService.open("solo-leveling", "1")).thenReturn(Optional.of(view));

        mvc.perform(get("/read/solo-leveling/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-type=\"MANHWA\"")))
                .andExpect(content().string(containsString("data-next-url=\"/read/solo-leveling/2\"")))
                .andExpect(content().string(not(containsString("data-prev-url"))))
                .andExpect(content().string(containsString("width=\"1000\" height=\"1500\"")))
                .andExpect(content().string(containsString("Страница 3 из 3")))
                .andExpect(content().string(containsString("<span>Дальше: Глава 2</span>")));
    }

    @Test
    void unknownChapterIs404() throws Exception {
        when(readerService.open(anyString(), anyString())).thenReturn(Optional.empty());

        mvc.perform(get("/read/solo-leveling/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound());
    }

    @Test
    void pluralizesRussianNouns() {
        assertEquals("тайтл", PageController.plural(1, "тайтл", "тайтла", "тайтлов"));
        assertEquals("тайтла", PageController.plural(3, "тайтл", "тайтла", "тайтлов"));
        assertEquals("тайтлов", PageController.plural(5, "тайтл", "тайтла", "тайтлов"));
        assertEquals("тайтлов", PageController.plural(11, "тайтл", "тайтла", "тайтлов"));
        assertEquals("тайтл", PageController.plural(21, "тайтл", "тайтла", "тайтлов"));
        assertEquals("тайтлов", PageController.plural(112, "тайтл", "тайтла", "тайтлов"));
    }
}
