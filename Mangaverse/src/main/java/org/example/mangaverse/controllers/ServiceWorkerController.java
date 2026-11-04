package org.example.mangaverse.controllers;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the service worker from the site root (so it controls every page) and never lets HTTP caches
 * keep an old copy, unlike static files which are cached for a year.
 */
@RestController
public class ServiceWorkerController {

    private final byte[] script;

    public ServiceWorkerController() throws IOException {
        this.script = new ClassPathResource("pwa/sw.js").getContentAsByteArray();
    }

    @GetMapping("/sw.js")
    public ResponseEntity<byte[]> serviceWorker() {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/javascript"))
                .cacheControl(CacheControl.noCache())
                .body(script);
    }
}
