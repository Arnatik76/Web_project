package org.example.mangaverse.models;

import org.springframework.data.annotation.Id;
import javax.validation.constraints.*;
import org.springframework.data.relational.core.mapping.Column;

public class Manga {

    @Id
    private Long id;

    @NotNull
    @Size(max = 50)
    private String title;

    @NotNull
    @Size(max = 50)
    private String author;

    @NotNull
    @Size(max = 50)
    private String genre;

    @NotNull
    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Float rating;

    @NotNull
    @Size(max = 500)
    @Column("imageUrl")
    private String imageUrl;

    public Manga(Long id, String title, String author, String genre, String description, float rating, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.rating = rating;
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

    public float getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
