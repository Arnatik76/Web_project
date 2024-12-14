package org.example.mangaverse.controllers;

import org.example.mangaverse.exceptions.MangaNotFoundException;
import org.example.mangaverse.models.ErrorDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(MangaNotFoundException.class)
    public ResponseEntity<ErrorDetails> exceptionMangaNotFoundHandler() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setMessage("Manga not found!");
        return ResponseEntity
                .status(114)
                .body(errorDetails);
    }
}
