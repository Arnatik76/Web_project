package org.example.mangaverse.controllers;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

import org.example.mangaverse.accounts.AccountUser;
import org.example.mangaverse.library.LibraryService;
import org.example.mangaverse.library.LibraryStatus;
import org.example.mangaverse.library.ProgressItem;
import org.example.mangaverse.library.Sync;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The signed-in user's library and reading progress. The browser is the source of truth; this is its sync backend. */
@RestController
@RequestMapping("/api/me")
public class MeApiController {

    public record StatusUpdate(LibraryStatus status, @PositiveOrZero long updatedAt) {
    }

    private final LibraryService library;

    public MeApiController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public Map<String, Object> me(@AuthenticationPrincipal AccountUser user) {
        return Map.of("name", user.getDisplayName(), "email", user.getUsername(), "admin", user.isAdmin());
    }

    @PostMapping("/sync")
    public Sync.State sync(@AuthenticationPrincipal AccountUser user, @Valid @RequestBody Sync.Request request) {
        return library.sync(user.getId(), request);
    }

    @PutMapping("/library/{slug}")
    public ResponseEntity<Void> setStatus(@AuthenticationPrincipal AccountUser user, @PathVariable String slug,
                                          @Valid @RequestBody StatusUpdate update) {
        if (!library.setStatus(user.getId(), slug, update.status(), update.updatedAt())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown title");
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/progress/{slug}")
    public ResponseEntity<Void> saveProgress(@AuthenticationPrincipal AccountUser user, @PathVariable String slug,
                                             @Valid @RequestBody ProgressItem item) {
        if (!slug.equals(item.slug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug mismatch");
        }
        if (!library.saveProgress(user.getId(), item)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown title");
        }
        return ResponseEntity.noContent().build();
    }
}
