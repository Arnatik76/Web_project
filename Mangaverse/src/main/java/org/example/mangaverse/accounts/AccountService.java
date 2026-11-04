package org.example.mangaverse.accounts;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements UserDetailsService {

    /** Thrown when someone signs up with an email that already has an account. */
    public static class EmailTakenException extends RuntimeException {
    }

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;

    public AccountService(UserRepository users, PasswordEncoder passwordEncoder,
                          @Value("${mangaverse.admin-email:}") String adminEmail) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = normalize(adminEmail);
    }

    @Override
    public AccountUser loadUserByUsername(String email) {
        return users.findByEmail(normalize(email))
                .map(AccountUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account"));
    }

    /** Creates an account. The address configured as MANGAVERSE_ADMIN_EMAIL becomes an administrator. */
    public AppUser register(String name, String email, String rawPassword) {
        String normalized = normalize(email);
        if (users.existsByEmail(normalized)) {
            throw new EmailTakenException();
        }
        String role = !adminEmail.isEmpty() && adminEmail.equals(normalized) ? AppUser.ADMIN : AppUser.USER;
        return users.save(new AppUser(null, normalized, name.strip(), passwordEncoder.encode(rawPassword), role, null));
    }

    static String normalize(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
