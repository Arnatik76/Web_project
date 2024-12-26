package org.example.mangaverse.controllers;

import org.example.mangaverse.exceptions.MangaNotFoundException;
import org.example.mangaverse.models.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(MangaNotFoundException.class)
    public ResponseEntity<ErrorDetails> exceptionMangaNotFoundHandler() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setMessage("Manga not found!");
        return ResponseEntity
                .status(404)
                .body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Invalid parameter: " + ex.getName() + ". Expected type: " + ex.getRequiredType().getSimpleName();
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ErrorDetails> exMethodArgumentTypeMismatchException() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setMessage("Manga not found!");
        return ResponseEntity
                .status(404)
                .body(errorDetails);
    }

}
