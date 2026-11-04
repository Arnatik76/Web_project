package org.example.mangaverse.library;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/** Reading position in one title. {@code title} and {@code cover} are filled in responses only. */
public record ProgressItem(
        @NotNull @Pattern(regexp = Sync.SLUG) String slug,
        @NotNull @Pattern(regexp = "\\d{1,5}(\\.\\d)?") String chapter,
        @Min(1) int page,
        @Min(1) @Max(5000) int total,
        boolean finished,
        @PositiveOrZero long updatedAt,
        String title,
        String cover
) {
    @AssertTrue
    boolean isPageWithinChapter() {
        return page <= total;
    }
}
