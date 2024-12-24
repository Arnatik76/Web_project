package org.example.mangaverse.controllers;

import java.util.List;
import java.util.logging.Logger;

import org.example.mangaverse.exceptions.MangaNotFoundException;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.services.MangaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manga")
public class MangaApiController {

    private static final Logger logger = Logger.getLogger(MangaApiController.class.getName());
    private final MangaService mangaService;

    public MangaApiController(MangaService mangaService) {
        this.mangaService = mangaService;
    }

    @GetMapping("/{mangaId}")
    public ResponseEntity<Manga> getManga(@PathVariable Long mangaId) {
        logger.info("GET /api/manga/" + mangaId);
        Manga manga = mangaService.getMangaById(mangaId);
        if (manga == null) {
            logger.info("Manga not found with id: " + mangaId);
            throw new MangaNotFoundException();
        } else {
            logger.info("Manga found: " + manga);
        }

        return ResponseEntity.ok(manga);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Manga>> getAllManga() {
        logger.info("GET /api/manga/all");
        List<Manga> mangaList = mangaService.getAllManga();
        return ResponseEntity.ok(mangaList);
    }

    @PostMapping("/new")
    public ResponseEntity<Manga> addManga(@RequestBody Manga manga) {
        logger.info("POST /api/manga/new");
        Manga newManga = mangaService.addManga(manga);
        return ResponseEntity.ok(newManga);
    }


    /*@GetMapping("/api/manga/{id}")
public ResponseEntity<byte[]> getMangaPdf(@PathVariable Long id) {
    try {
        // Путь к PDF файлу на сервере
        String filePath = "path/to/manga_" + id + ".pdf";
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build(); // Если файл не найден
        }

        // Чтение файла в массив байтов
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // Формирование HTTP ответа
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("inline")
                .filename("manga_" + id + ".pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Обработка ошибок
    }
}
*/

//    @GetMapping("/stalker_3/a")
//    public ModelAndView stalker_3_a(@RequestParam(name = "chapter", required = false) int chapter,
//                                    @RequestParam(name = "page", required = false) int page) {
//        ModelAndView modelAndView = new ModelAndView("read");
//        modelAndView.addObject("chapter", chapter);
//        modelAndView.addObject("page", page);
//        return modelAndView;
//    }
}
