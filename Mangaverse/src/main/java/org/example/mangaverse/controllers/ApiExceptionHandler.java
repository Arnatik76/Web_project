package org.example.mangaverse.controllers;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns API errors (404, 403, validation, bad parameters) into RFC 7807 problem+json responses.
 * Scoped to the API so that HTML pages keep rendering the regular error templates.
 */
@RestControllerAdvice(assignableTypes = {MangaApiController.class, MeApiController.class})
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
}
