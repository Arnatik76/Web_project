package org.example.mangaverse.services;

import static org.example.mangaverse.TestData.manga;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.repositories.MangaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class MangaServiceTest {

    private final MangaRepository repository = mock(MangaRepository.class);
    private final MangaService service = new MangaService(repository);

    private final Manga naruto = manga(1, "Naruto", "Боевик", MangaType.MANGA, "4.7");
    private final Manga solo = manga(2, "Solo Leveling", "Фэнтези", MangaType.MANHWA, "3.7");
    private final Manga bleach = manga(3, "Bleach", "Боевик", MangaType.MANGA, "4.3");

    @Test
    void escapesLikeWildcards() {
        service.search("50%_off\\");
        verify(repository).search("%50\\%\\_off\\\\%");
    }

    @Test
    void blankSearchDoesNotHitDatabase() {
        assertEquals(List.of(), service.search("   "));
        verify(repository, never()).search(ArgumentMatchers.anyString());
    }

    @Test
    void catalogFiltersAndSorts() {
        when(repository.findAllByPopularity()).thenReturn(List.of(naruto, bleach, solo));

        assertEquals(List.of(bleach, naruto), service.catalog(null, "Боевик", null, CatalogSort.TITLE));
        assertEquals(List.of(solo), service.catalog(null, null, MangaType.MANHWA, CatalogSort.POPULAR));
        assertEquals(List.of(bleach, solo, naruto), service.catalog(null, null, null, CatalogSort.NEW));
    }

    @Test
    void genreFilterMatchesSecondaryGenres() {
        Manga hybrid = new Manga(4L, "Hybrid", "hybrid", "A", "Драма", List.of("Драма", "Боевик"),
                MangaType.MANGA, "d", new java.math.BigDecimal("4.0"), "/images/x.jpg", null);
        when(repository.findAllByPopularity()).thenReturn(List.of(naruto, hybrid, solo));

        assertEquals(List.of(naruto, hybrid), service.catalog(null, "Боевик", null, CatalogSort.POPULAR));
    }

    @Test
    void addGeneratesUniqueSlug() {
        when(repository.existsBySlug("solo-leveling")).thenReturn(true);
        when(repository.existsBySlug("solo-leveling-2")).thenReturn(true);
        when(repository.save(ArgumentMatchers.any(Manga.class))).thenAnswer(inv -> inv.getArgument(0));

        Manga saved = service.add(solo.withSlug(null));
        assertEquals("solo-leveling-3", saved.slug());
    }

    @Test
    void similarExcludesItselfAndDuplicates() {
        when(repository.findByGenre("Боевик")).thenReturn(List.of(naruto, bleach));
        when(repository.findByType("MANGA")).thenReturn(List.of(naruto, bleach));

        assertEquals(List.of(bleach), service.similar(naruto, 6));
    }
}
