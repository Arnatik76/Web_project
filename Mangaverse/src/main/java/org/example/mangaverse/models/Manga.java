package org.example.mangaverse.models;

import org.springframework.data.annotation.Id;

public class Manga {

    @Id
    private Long id;

    private String title;
    private String author;
    private String genre;
    private String description;

    public Manga(Long id, String title, String author, String genre, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
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
}
