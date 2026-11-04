package org.example.mangaverse.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** One image of a chapter. Dimensions are stored so the reader can reserve space before the image loads. */
@Table("page")
public record ChapterPage(
        @Id Long id,
        Long chapterId,
        int idx,
        String path,
        int width,
        int height
) {
}
