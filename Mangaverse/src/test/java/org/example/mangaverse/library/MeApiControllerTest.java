package org.example.mangaverse.library;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.example.mangaverse.accounts.AccountUser;
import org.example.mangaverse.accounts.AppUser;
import org.example.mangaverse.config.SecurityConfig;
import org.example.mangaverse.controllers.MeApiController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeApiController.class)
@Import(SecurityConfig.class)
class MeApiControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    LibraryService library;

    @MockitoBean
    UserDetailsService userDetailsService;

    private final AccountUser reader = new AccountUser(new AppUser(7L, "reader@example.com", "Reader", "{noop}x", AppUser.USER, null));

    @Test
    void requiresSignInWithoutRedirect() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void returnsProfile() throws Exception {
        mvc.perform(get("/api/me").with(user(reader)))
                .andExpect(jsonPath("$.name").value("Reader"))
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void syncMergesAndReturnsState() throws Exception {
        when(library.sync(eq(7L), any())).thenReturn(new Sync.State(
                List.of(new LibraryItem("solo-leveling", LibraryStatus.READING, 1000, "Solo Leveling", "/images/x.webp")),
                List.of(new ProgressItem("solo-leveling", "2", 3, 8, false, 1000, "Solo Leveling", "/images/x.webp"))));

        mvc.perform(post("/api/me/sync").with(user(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"library":[{"slug":"solo-leveling","status":"READING","updatedAt":1000}],
                                 "progress":[{"slug":"solo-leveling","chapter":"2","page":3,"total":8,"finished":false,"updatedAt":1000}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.library[0].status").value("READING"))
                .andExpect(jsonPath("$.progress[0].page").value(3));
    }

    @Test
    void syncWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/api/me/sync").with(user(reader)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"library\":[],\"progress\":[]}"))
                .andExpect(status().isForbidden());
        verify(library, never()).sync(anyLong(), any());
    }

    @Test
    void rejectsInvalidProgress() throws Exception {
        mvc.perform(put("/api/me/progress/solo-leveling").with(user(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"solo-leveling\",\"chapter\":\"2\",\"page\":9,\"total\":8,\"finished\":false,\"updatedAt\":1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/me/progress/solo-leveling").with(user(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"../etc\",\"chapter\":\"2\",\"page\":1,\"total\":8,\"finished\":false,\"updatedAt\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownTitleIs404() throws Exception {
        when(library.setStatus(eq(7L), eq("nope"), any(), anyLong())).thenReturn(false);
        mvc.perform(put("/api/me/library/nope").with(user(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PLANNED\",\"updatedAt\":5}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removingFromLibrarySendsNullStatus() throws Exception {
        when(library.setStatus(7L, "solo-leveling", null, 5)).thenReturn(true);
        mvc.perform(put("/api/me/library/solo-leveling").with(user(reader)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":null,\"updatedAt\":5}"))
                .andExpect(status().isNoContent());
    }
}
