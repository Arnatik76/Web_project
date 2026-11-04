package org.example.mangaverse.accounts;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Signed-in principal: Spring's {@link User} plus our id and display name. */
public class AccountUser extends User {

    private final long id;
    private final String displayName;

    public AccountUser(AppUser user) {
        super(user.email(), user.passwordHash(), List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
        this.id = user.id();
        this.displayName = user.name();
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAdmin() {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + AppUser.ADMIN));
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
