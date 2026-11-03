package org.example.mangaverse.library;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * A library shelf entry as exchanged with the browser. {@code status == null} means "removed".
 * {@code title} and {@code cover} are filled in responses and ignored in requests.
 */
public record LibraryItem(
        @NotNull @Pattern(regexp = Sync.SLUG) String slug,
        LibraryStatus status,
        @PositiveOrZero long updatedAt,
        String title,
        String cover
) {
}
