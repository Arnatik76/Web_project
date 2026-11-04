package org.example.mangaverse.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.example.mangaverse.TestData;
import org.example.mangaverse.models.Chapter;
import org.example.mangaverse.models.ChapterPage;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.repositories.ChapterRepository;
import org.example.mangaverse.repositories.PageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ChapterUploadServiceTest {

    @TempDir
    Path media;

    private final ChapterRepository chapters = mock(ChapterRepository.class);
    private final PageRepository pages = mock(PageRepository.class);
    private final Manga solo = TestData.manga(2, "Solo Leveling", "Фэнтези", MangaType.MANHWA, "3.7");

    @Test
    void storesPagesInNaturalOrderWithRealDimensions() throws IOException {
        ChapterUploadService service = new ChapterUploadService(chapters, pages, media.toString());
        when(chapters.save(any(Chapter.class))).thenAnswer(inv -> {
            Chapter c = inv.getArgument(0);
            return new Chapter(55L, c.mangaId(), c.number(), c.title(), c.publishedOn());
        });
        List<ChapterPage> saved = new ArrayList<>();
        when(pages.save(any(ChapterPage.class))).thenAnswer(inv -> { saved.add(inv.getArgument(0)); return inv.getArgument(0); });

        service.upload(solo, new BigDecimal("3"), "  Title  ", List.of(png("10.png", 10, 30), png("2.png", 20, 30), png("1.png", 30, 30)));

        assertEquals(List.of(30, 20, 10), saved.stream().map(ChapterPage::width).toList());
        assertEquals("/media/uploads/chapters/55/1.png", saved.get(0).path());
        assertTrue(Files.exists(media.resolve("chapters/55/3.png")));
    }

    @Test
    void rejectsExistingChapterBeforeWritingAnything() {
        ChapterUploadService service = new ChapterUploadService(chapters, pages, media.toString());
        when(chapters.findByMangaIdAndNumber(2L, new BigDecimal("3"))).thenReturn(Optional.of(TestData.chapter(1, 2, "3")));
        var error = assertThrows(ChapterUploadService.UploadException.class,
                () -> service.upload(solo, new BigDecimal("3"), null, List.of(png("1.png", 10, 10))));
        assertEquals("Глава 3 уже есть", error.getMessage());
        verify(chapters, never()).save(any());
    }

    @Test
    void rejectsFilesThatAreNotImages() {
        ChapterUploadService service = new ChapterUploadService(chapters, pages, media.toString());
        MultipartFile fake = new MockMultipartFile("pages", "evil.png", "image/png", "<script>".getBytes());
        assertThrows(ChapterUploadService.UploadException.class,
                () -> service.upload(solo, BigDecimal.ONE, null, List.of(fake)));
        verify(chapters, never()).save(any());
    }

    private static MultipartFile png(String name, int width, int height) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", out);
            return new MockMultipartFile("pages", name, "image/png", out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
