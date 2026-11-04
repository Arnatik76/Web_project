package org.example.mangaverse.admin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.example.mangaverse.models.Chapter;
import org.example.mangaverse.models.ChapterPage;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.repositories.ChapterRepository;
import org.example.mangaverse.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Stores an uploaded chapter: validates every image, writes files under the media directory, records pages. */
@Service
public class ChapterUploadService {

    public static class UploadException extends RuntimeException {
        public UploadException(String message) {
            super(message);
        }
    }

    static final int MAX_PAGES = 300;
    static final long MAX_PAGE_BYTES = 15L * 1024 * 1024;
    private static final int MAX_SIDE = 20_000;
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private final ChapterRepository chapters;
    private final PageRepository pages;
    private final Path mediaDir;

    public ChapterUploadService(ChapterRepository chapters, PageRepository pages,
                                @Value("${mangaverse.media-dir:media}") String mediaDir) {
        this.chapters = chapters;
        this.pages = pages;
        this.mediaDir = Path.of(mediaDir).toAbsolutePath().normalize();
    }

    /** Pages are ordered by file name with numbers compared numerically: 2.jpg comes before 10.jpg. */
    @Transactional
    public Chapter upload(Manga manga, BigDecimal number, String title, List<MultipartFile> files) {
        String label = number.stripTrailingZeros().toPlainString();
        if (chapters.findByMangaIdAndNumber(manga.id(), number).isPresent()) {
            throw new UploadException("Глава " + label + " уже есть");
        }
        List<MultipartFile> images = files.stream().filter(f -> !f.isEmpty())
                .sorted(Comparator.comparing(f -> naturalKey(f.getOriginalFilename()))).toList();
        if (images.isEmpty()) {
            throw new UploadException("Добавьте хотя бы одну страницу");
        }
        if (images.size() > MAX_PAGES) {
            throw new UploadException("Не больше " + MAX_PAGES + " страниц в главе");
        }

        List<byte[]> contents = new ArrayList<>();
        List<ImageInfo> infos = new ArrayList<>();
        for (MultipartFile file : images) {
            if (file.getSize() > MAX_PAGE_BYTES) {
                throw new UploadException("Файл «" + file.getOriginalFilename() + "» больше 15 МБ");
            }
            byte[] bytes = bytes(file);
            ImageInfo info = ImageInfo.read(bytes).filter(i -> i.width() <= MAX_SIDE && i.height() <= MAX_SIDE)
                    .orElseThrow(() -> new UploadException("«" + file.getOriginalFilename() + "» — не JPEG, PNG или WebP"));
            contents.add(bytes);
            infos.add(info);
        }

        Chapter chapter;
        try {
            String cleanTitle = title == null || title.isBlank() ? null : title.strip();
            chapter = chapters.save(new Chapter(null, manga.id(), number, cleanTitle, LocalDate.now()));
        } catch (DbActionExecutionException e) {
            // Two uploads of the same chapter at once: the unique constraint decides.
            if (e.getCause() instanceof DuplicateKeyException) {
                throw new UploadException("Глава " + label + " уже есть");
            }
            throw e;
        }

        Path dir = mediaDir.resolve("chapters").resolve(String.valueOf(chapter.id()));
        try {
            Files.createDirectories(dir);
            for (int i = 0; i < contents.size(); i++) {
                ImageInfo info = infos.get(i);
                String name = (i + 1) + "." + info.extension();
                Files.write(dir.resolve(name), contents.get(i));
                pages.save(new ChapterPage(null, chapter.id(), i + 1,
                        "/media/uploads/chapters/" + chapter.id() + "/" + name, info.width(), info.height()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return chapter;
    }

    private static byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** "page 2.jpg" → "page 0000000002.jpg", so lexical order matches human order. */
    static String naturalKey(String name) {
        String safe = name == null ? "" : name.toLowerCase();
        return DIGITS.matcher(safe)
                .replaceAll(m -> String.format("%010d", Long.parseLong(m.group().length() > 10 ? m.group().substring(0, 10) : m.group())));
    }
}
