package org.example.mangaverse.accounts;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRepository extends ListCrudRepository<AppUser, Long> {

    @Query("SELECT * FROM app_user WHERE email = :email")
    Optional<AppUser> findByEmail(String email);

    @Query("SELECT count(*) > 0 FROM app_user WHERE email = :email")
    boolean existsByEmail(String email);
}
