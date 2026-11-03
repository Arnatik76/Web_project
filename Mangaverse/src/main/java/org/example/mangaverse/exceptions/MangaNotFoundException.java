package org.example.mangaverse.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MangaNotFoundException extends ResponseStatusException {

    public MangaNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Manga " + id + " not found");
    }
}
