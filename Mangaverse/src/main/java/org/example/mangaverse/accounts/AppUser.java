package org.example.mangaverse.accounts;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("app_user")
public record AppUser(
        @Id Long id,
        String email,
        String name,
        String passwordHash,
        String role,
        @ReadOnlyProperty Instant createdAt
) {
    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
}
