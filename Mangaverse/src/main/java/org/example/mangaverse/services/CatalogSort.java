package org.example.mangaverse.services;

import java.util.Comparator;

import org.example.mangaverse.models.Manga;

public enum CatalogSort {
    POPULAR("Популярные", Comparator.comparing(Manga::rating).reversed().thenComparing(Manga::title)),
    NEW("Новые", Comparator.comparing(Manga::id).reversed()),
    TITLE("А–Я", Comparator.comparing(Manga::title, String.CASE_INSENSITIVE_ORDER));

    private final String label;
    private final Comparator<Manga> comparator;

    CatalogSort(String label, Comparator<Manga> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Manga> comparator() {
        return comparator;
    }

    public static CatalogSort parse(String value) {
        if (value != null) {
            for (CatalogSort sort : values()) {
                if (sort.name().equalsIgnoreCase(value.trim())) {
                    return sort;
                }
            }
        }
        return POPULAR;
    }
}
