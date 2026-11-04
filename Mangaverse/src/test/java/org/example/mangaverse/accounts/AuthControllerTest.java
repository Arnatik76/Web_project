package org.example.mangaverse.accounts;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import org.example.mangaverse.config.SecurityConfig;
import org.example.mangaverse.controllers.AuthController;
import org.example.mangaverse.web.Covers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, Covers.class})
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    AccountService accounts;

    private AppUser stored;

    @BeforeEach
    void setUp() {
        stored = new AppUser(7L, "reader@example.com", "Reader", passwordEncoder.encode("correct horse"), AppUser.USER, null);
        when(accounts.loadUserByUsername("reader@example.com")).thenAnswer(inv -> new AccountUser(stored));
    }

    @Test
    void signsInWithEmailAndPassword() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("email", "reader@example.com").param("password", "correct horse"))
                .andExpect(redirectedUrl("/library?synced=1"))
                .andExpect(authenticated().withUsername("reader@example.com"));
    }

    @Test
    void wrongPasswordShowsError() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("email", "reader@example.com").param("password", "nope"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void loginWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/login").param("email", "reader@example.com").param("password", "correct horse"))
                .andExpect(status().isForbidden());
    }

    @Test
    void signupValidatesInput() throws Exception {
        mvc.perform(post("/signup").with(csrf()).param("name", "").param("email", "not-an-email").param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Как к вам обращаться?")))
                .andExpect(content().string(containsString("Похоже, в адресе опечатка")))
                .andExpect(content().string(containsString("от 8 до 72 символов")));
        verify(accounts, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void signupRejectsPasswordsLongerThan72Bytes() throws Exception {
        String cyrillic = "пароль".repeat(7); // 42 characters, 84 bytes in UTF-8
        mvc.perform(post("/signup").with(csrf()).param("name", "A").param("email", "a@example.com").param("password", cyrillic))
                .andExpect(content().string(containsString("Слишком длинный пароль")));
    }

    @Test
    void signupWithTakenEmailSuggestsSigningIn() throws Exception {
        when(accounts.register(anyString(), eq("reader@example.com"), anyString())).thenThrow(new AccountService.EmailTakenException());
        mvc.perform(post("/signup").with(csrf()).param("name", "R").param("email", "reader@example.com").param("password", "correct horse"))
                .andExpect(content().string(containsString("уже зарегистрирован")));
    }

    @Test
    void signupSignsTheUserIn() throws Exception {
        when(accounts.register("Reader", "reader@example.com", "correct horse")).thenReturn(stored);
        mvc.perform(post("/signup").with(csrf()).param("name", "Reader").param("email", "reader@example.com").param("password", "correct horse"))
                .andExpect(redirectedUrl("/library?synced=1"))
                .andExpect(authenticated());
    }

    @Test
    void libraryPageIsPublicAndOffersSignIn() throws Exception {
        mvc.perform(get("/library"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Библиотека хранится на этом устройстве")))
                .andExpect(content().string(containsString("name=\"mv-user\" content=\"0\"")))
                // No CSRF token (and so no session) for anonymous visitors.
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("name=\"_csrf\""))));
    }
}
