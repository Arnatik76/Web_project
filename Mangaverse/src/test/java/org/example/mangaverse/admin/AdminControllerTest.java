package org.example.mangaverse.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.example.mangaverse.TestData;
import org.example.mangaverse.accounts.AccountUser;
import org.example.mangaverse.accounts.AppUser;
import org.example.mangaverse.config.SecurityConfig;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.services.MangaService;
import org.example.mangaverse.web.Covers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, Covers.class})
class AdminControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MangaService mangaService;

    @MockitoBean
    ChapterUploadService uploads;

    @MockitoBean
    UserDetailsService userDetailsService;

    private final AccountUser admin = new AccountUser(new AppUser(1L, "a@example.com", "Admin", "{noop}x", AppUser.ADMIN, null));
    private final AccountUser reader = new AccountUser(new AppUser(2L, "r@example.com", "Reader", "{noop}x", AppUser.USER, null));
    private final Manga solo = TestData.manga(2, "Solo Leveling", "Фэнтези", MangaType.MANHWA, "3.7");

    @Test
    void anonymousIsSentToLogin() throws Exception {
        // Browsers ask for HTML and are redirected to the login page.
        mvc.perform(get("/admin/chapters/new").accept(MediaType.TEXT_HTML)).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void regularUserIsForbidden() throws Exception {
        mvc.perform(get("/admin/chapters/new").with(user(reader))).andExpect(status().isForbidden());
    }

    @Test
    void adminSeesTheForm() throws Exception {
        when(mangaService.popular()).thenReturn(List.of(solo));
        mvc.perform(get("/admin/chapters/new").with(user(admin))).andExpect(status().isOk());
    }

    @Test
    void uploadRedirectsToTheNewChapter() throws Exception {
        when(mangaService.findBySlug("solo-leveling")).thenReturn(Optional.of(solo));
        MockMultipartFile page = new MockMultipartFile("pages", "1.png", "image/png", new byte[] {1});
        mvc.perform(multipart("/admin/chapters").file(page).with(user(admin)).with(csrf())
                        .param("manga", "solo-leveling").param("number", "4.5"))
                .andExpect(redirectedUrl("/read/solo-leveling/4.5"));
    }

    @Test
    void uploadErrorsReturnToTheFormWithAMessage() throws Exception {
        when(mangaService.findBySlug("solo-leveling")).thenReturn(Optional.of(solo));
        when(uploads.upload(any(), any(BigDecimal.class), any(), any()))
                .thenThrow(new ChapterUploadService.UploadException("Глава 4 уже есть"));
        MockMultipartFile page = new MockMultipartFile("pages", "1.png", "image/png", new byte[] {1});
        mvc.perform(multipart("/admin/chapters").file(page).with(user(admin)).with(csrf())
                        .param("manga", "solo-leveling").param("number", "4"))
                .andExpect(redirectedUrl("/admin/chapters/new?manga=solo-leveling"))
                .andExpect(flash().attribute("error", "Глава 4 уже есть"));
    }

    @Test
    void regularUserCannotUpload() throws Exception {
        MockMultipartFile page = new MockMultipartFile("pages", "1.png", "image/png", new byte[] {1});
        mvc.perform(multipart("/admin/chapters").file(page).with(user(reader)).with(csrf())
                        .param("manga", "solo-leveling").param("number", "4"))
                .andExpect(status().isForbidden());
    }
}
