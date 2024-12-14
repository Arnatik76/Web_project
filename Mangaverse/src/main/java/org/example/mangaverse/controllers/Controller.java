package org.example.mangaverse.controllers;

import java.util.logging.Logger;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.services.MangaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@RestController
@RequestMapping("/api/manga")
public class Controller {

    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private final MangaService ms;

    public Controller(MangaService ms) {
        this.ms = ms;
    }

    @GetMapping("/{mangaId}")
    public ResponseEntity<Manga> getManga(@PathVariable Long mangaId) {
        logger.info("GET /api/manga/" + mangaId);
        Manga manga = ms.getMangaById(mangaId);
        if (manga == null) {
            logger.info("Manga not found with id: " + mangaId);
        } else {
            logger.info("Manga found: " + manga);
        }

        return manga != null ? ResponseEntity.ok(manga) : ResponseEntity.notFound().build();
    }


//    @GetMapping("/stalker_3/a")
//    public ModelAndView stalker_3_a(@RequestParam(name = "chapter", required = false) int chapter,
//                                    @RequestParam(name = "page", required = false) int page) {
//        ModelAndView modelAndView = new ModelAndView("read");
//        modelAndView.addObject("chapter", chapter);
//        modelAndView.addObject("page", page);
//        return modelAndView;
//    }
}
