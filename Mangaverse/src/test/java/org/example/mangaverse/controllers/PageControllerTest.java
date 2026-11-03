package org.example.mangaverse.controllers;

import static org.example.mangaverse.TestData.manga;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.services.CatalogSort;
import org.example.mangaverse.services.MangaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PageController.class)
class PageControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MangaService mangaService;

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
                .andExpect(content().string(containsString("Популярное")))
                .andExpect(content().string(containsString("/title/2")));
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
    void titlePageRendersDetails() throws Exception {
        when(mangaService.findById(2)).thenReturn(Optional.of(solo));
        when(mangaService.similar(eq(solo), anyInt())).thenReturn(List.of(naruto));

        mvc.perform(get("/title/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Solo Leveling")))
                .andExpect(content().string(containsString("Похожие")));
    }

    @Test
    void unknownTitleIs404() throws Exception {
        when(mangaService.findById(404)).thenReturn(Optional.empty());

        mvc.perform(get("/title/404").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound());
    }

    @Test
    void pluralizesRussianNouns() {
        assertPlural(1, "тайтл");
        assertPlural(3, "тайтла");
        assertPlural(5, "тайтлов");
        assertPlural(11, "тайтлов");
        assertPlural(21, "тайтл");
        assertPlural(112, "тайтлов");
    }

    private static void assertPlural(int n, String expected) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, PageController.plural(n, "тайтл", "тайтла", "тайтлов"));
    }
}
