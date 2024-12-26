package org.example.mangaverse.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public class Manga {

    @Id
    private Long id;

    private String title;
    private String author;
    private String genre;
    private String description;

    @Column("imageUrl")
    private String imageUrl;

    public Manga(Long id, String title, String author, String genre, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
