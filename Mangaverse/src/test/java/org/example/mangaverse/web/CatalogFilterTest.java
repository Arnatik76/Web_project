package org.example.mangaverse.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.services.CatalogSort;
import org.junit.jupiter.api.Test;

class CatalogFilterTest {

    @Test
    void defaultFilterHasCleanUrl() {
        CatalogFilter filter = new CatalogFilter(" ", "", null, null);
        assertEquals("/catalog", filter.getUrl());
        assertFalse(filter.isFiltered());
    }

    @Test
    void changingOneFilterKeepsTheOthers() {
        CatalogFilter filter = new CatalogFilter("solo", null, MangaType.MANHWA, CatalogSort.TITLE);
        assertEquals("/catalog?q=solo&genre=%D0%A4%D1%8D%D0%BD%D1%82%D0%B5%D0%B7%D0%B8&type=manhwa&sort=title",
                filter.withGenre("Фэнтези").getUrl());
        assertEquals("/catalog?type=manhwa&sort=title", filter.withoutQuery().getUrl());
        assertTrue(filter.isFiltered());
    }

    @Test
    void encodesReservedCharacters() {
        assertEquals("/catalog?q=a%26b%3Dc", new CatalogFilter("a&b=c", null, null, null).getUrl());
    }
}
