package org.example.mangaverse.controllers;

import static org.example.mangaverse.TestData.manga;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.services.MangaService;
import org.example.mangaverse.services.ReaderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MangaApiController.class)
@TestPropertySource(properties = "mangaverse.admin-token=secret")
class MangaApiControllerTest {

    private static final String VALID_BODY = """
            {"title":"Berserk","author":"Kentaro Miura","genre":"Драма","type":"MANGA",
             "description":"Dark fantasy","rating":4.9,"imageUrl":"/images/berserk.jpg"}
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MangaService mangaService;

    @MockitoBean
    ReaderService readerService;

    @Test
    void returnsMangaById() throws Exception {
        when(mangaService.findById(1)).thenReturn(Optional.of(manga(1, "Naruto", "Боевик", MangaType.MANGA, "4.7")));

        mvc.perform(get("/api/manga/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Naruto"))
                .andExpect(jsonPath("$.type").value("MANGA"));
    }

    @Test
    void unknownIdIsProblemJson404() throws Exception {
        when(mangaService.findById(42)).thenReturn(Optional.empty());

        mvc.perform(get("/api/manga/42"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void nonNumericIdIs400() throws Exception {
        mvc.perform(get("/api/manga/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void searchAcceptsBothParameterNames() throws Exception {
        when(mangaService.search("solo")).thenReturn(List.of(manga(10, "Solo Leveling", "Фэнтези", MangaType.MANHWA, "3.7")));

        mvc.perform(get("/api/manga/search").param("query", "solo")).andExpect(jsonPath("$[0].title").value("Solo Leveling"));
        mvc.perform(get("/api/manga/search").param("q", "solo")).andExpect(jsonPath("$[0].title").value("Solo Leveling"));
    }

    @Test
    void writeWithoutTokenIsForbiddenBeforeValidation() throws Exception {
        mvc.perform(post("/api/manga/new").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verify(mangaService, never()).add(any());
    }

    @Test
    void writeWithWrongTokenIsForbidden() throws Exception {
        mvc.perform(post("/api/manga/new").header("X-Admin-Token", "nope")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsScriptImageUrl() throws Exception {
        String body = VALID_BODY.replace("/images/berserk.jpg", "javascript:alert(1)");
        mvc.perform(post("/api/manga/new").header("X-Admin-Token", "secret")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verify(mangaService, never()).add(any());
    }

    @Test
    void createsMangaWithValidToken() throws Exception {
        when(mangaService.add(any(Manga.class))).thenAnswer(inv -> {
            Manga m = inv.getArgument(0);
            return new Manga(99L, m.title(), "berserk", m.author(), m.genre(), m.type(), m.description(), m.rating(), m.imageUrl(), null);
        });

        mvc.perform(post("/api/manga/new").header("X-Admin-Token", "secret")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/manga/99"))
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.slug").value("berserk"));
    }
}
