package org.example.mangaverse.models;

/** Format of a title. Drives the default reader mode: paged for manga, vertical scroll for manhwa and manhua. */
public enum MangaType {
    MANGA("Манга"),
    MANHWA("Манхва"),
    MANHUA("Маньхуа");

    private final String label;

    MangaType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Lenient parsing for query parameters: unknown or blank values yield {@code null}. */
    public static MangaType parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
