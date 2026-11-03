package org.example.mangaverse.admin;

import java.math.BigDecimal;
import java.util.List;

import org.example.mangaverse.models.Manga;
import org.example.mangaverse.services.MangaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin-only (see SecurityConfig): upload chapters. */
@Controller
public class AdminController {

    private final MangaService mangaService;
    private final ChapterUploadService uploads;

    public AdminController(MangaService mangaService, ChapterUploadService uploads) {
        this.mangaService = mangaService;
        this.uploads = uploads;
    }

    @GetMapping("/admin/chapters/new")
    public String form(@RequestParam(required = false) String manga, Model model) {
        model.addAttribute("titles", mangaService.popular().stream()
                .sorted((a, b) -> a.title().compareToIgnoreCase(b.title())).toList());
        model.addAttribute("selected", manga);
        return "admin/chapter-new";
    }

    @PostMapping("/admin/chapters")
    public String upload(@RequestParam String manga, @RequestParam String number,
                         @RequestParam(required = false) String title,
                         @RequestParam("pages") List<MultipartFile> pages,
                         RedirectAttributes redirect) {
        Manga target = mangaService.findBySlug(manga).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!number.matches("\\d{1,5}(\\.\\d)?")) {
            return fail(redirect, manga, "Номер главы — число, например 12 или 12.5");
        }
        try {
            uploads.upload(target, new BigDecimal(number), title, pages);
        } catch (ChapterUploadService.UploadException e) {
            return fail(redirect, manga, e.getMessage());
        }
        return "redirect:/read/" + target.slug() + "/" + new BigDecimal(number).stripTrailingZeros().toPlainString();
    }

    private static String fail(RedirectAttributes redirect, String manga, String message) {
        redirect.addFlashAttribute("error", message);
        redirect.addAttribute("manga", manga);
        return "redirect:/admin/chapters/new";
    }
}
