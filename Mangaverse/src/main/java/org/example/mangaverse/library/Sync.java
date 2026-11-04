package org.example.mangaverse.library;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Payloads of POST /api/me/sync: the device sends everything it has, the server answers with the merged state. */
public final class Sync {

    static final String SLUG = "[a-z0-9]+(-[a-z0-9]+)*";

    private Sync() {
    }

    public record Request(
            @NotNull @Size(max = 1000) List<@Valid LibraryItem> library,
            @NotNull @Size(max = 1000) List<@Valid ProgressItem> progress
    ) {
    }

    public record State(List<LibraryItem> library, List<ProgressItem> progress) {
    }
}
