package org.example.mangaverse.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class AccountServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final AccountService service = new AccountService(users, encoder, " Admin@Example.com ");

    @Test
    void normalizesEmailAndHashesPassword() {
        when(users.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));
        AppUser user = service.register("  Ann ", " Ann@Example.COM ", "secret-password");
        assertEquals("ann@example.com", user.email());
        assertEquals("Ann", user.name());
        assertEquals(AppUser.USER, user.role());
        assertTrue(encoder.matches("secret-password", user.passwordHash()));
    }

    @Test
    void configuredEmailBecomesAdmin() {
        when(users.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(AppUser.ADMIN, service.register("Boss", "admin@example.com", "secret-password").role());
    }

    @Test
    void rejectsDuplicateEmail() {
        when(users.existsByEmail("ann@example.com")).thenReturn(true);
        assertThrows(AccountService.EmailTakenException.class, () -> service.register("Ann", "ANN@example.com", "secret-password"));
    }

    @Test
    void loadsUserCaseInsensitively() {
        when(users.findByEmail("ann@example.com")).thenReturn(Optional.of(
                new AppUser(3L, "ann@example.com", "Ann", "{noop}x", AppUser.ADMIN, null)));
        AccountUser user = service.loadUserByUsername("ANN@example.com");
        assertEquals(3L, user.getId());
        assertTrue(user.isAdmin());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost@example.com"));
    }
}
