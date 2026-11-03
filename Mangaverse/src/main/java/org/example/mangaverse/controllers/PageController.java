package org.example.mangaverse.controllers;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.example.mangaverse.models.Chapter;
import org.example.mangaverse.models.Manga;
import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.services.CatalogSort;
import org.example.mangaverse.services.MangaService;
import org.example.mangaverse.services.ReaderService;
import org.example.mangaverse.web.CatalogFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/** Server-rendered pages. Thymeleaf escapes every value, so catalog data can never inject markup. */
@Controller
public class PageController {

    private static final int SHELF_SIZE = 12;
    private static final int GENRE_SHELVES = 3;

    private final MangaService mangaService;
    private final ReaderService readerService;
    private final int catalogPageSize;

    public PageController(MangaService mangaService, ReaderService readerService,
                          @Value("${mangaverse.catalog.page-size:24}") int catalogPageSize) {
        this.mangaService = mangaService;
        this.readerService = readerService;
        this.catalogPageSize = catalogPageSize;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Manga> popular = mangaService.popular();
        List<String> genres = mangaService.genres();

        Map<String, List<Manga>> genreShelves = new LinkedHashMap<>();
        for (String genre : genres.subList(0, Math.min(GENRE_SHELVES, genres.size()))) {
            genreShelves.put(genre, limit(mangaService.byGenre(genre)));
        }

        model.addAttribute("featured", featuredOfTheWeek(popular));
        model.addAttribute("popular", limit(popular));
        model.addAttribute("manhwa", limit(mangaService.byType(MangaType.MANHWA)));
        model.addAttribute("genreShelves", genreShelves);
        model.addAttribute("genres", genres);
        model.addAttribute("nav", "home");
        return "home";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) String q,
                          @RequestParam(required = false) String genre,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false) String sort,
                          @RequestParam(defaultValue = "1") int page,
                          Model model) {
        CatalogFilter filter = new CatalogFilter(q, genre, MangaType.parse(type), CatalogSort.parse(sort));
        List<Manga> results = mangaService.catalog(filter.q(), filter.genre(), filter.type(), filter.sort());
        int pages = Math.max(1, (results.size() + catalogPageSize - 1) / catalogPageSize);
        int current = Math.min(Math.max(page, 1), pages);
        int from = (current - 1) * catalogPageSize;
        model.addAttribute("filter", filter);
        model.addAttribute("results", results.subList(from, Math.min(from + catalogPageSize, results.size())));
        model.addAttribute("page", current);
        model.addAttribute("nextPageUrl", current < pages ? filter.pageUrl(current + 1) : null);
        model.addAttribute("resultsLabel", results.size() + " " + plural(results.size(), "тайтл", "тайтла", "тайтлов"));
        model.addAttribute("genres", mangaService.genres());
        model.addAttribute("types", MangaType.values());
        model.addAttribute("sorts", CatalogSort.values());
        model.addAttribute("nav", "catalog");
        return "catalog";
    }

    /** Title page by slug. Old numeric links (/title/10) permanently redirect to the slug URL. */
    @GetMapping("/title/{key}")
    public ModelAndView title(@PathVariable String key) {
        if (key.matches("\\d{1,18}")) {
            Manga manga = mangaService.findById(Long.parseLong(key)).orElseThrow(PageController::notFound);
            RedirectView redirect = new RedirectView("/title/" + manga.slug());
            redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            return new ModelAndView(redirect);
        }
        Manga manga = mangaService.findBySlug(key).orElseThrow(PageController::notFound);
        List<Chapter> chapters = readerService.chapters(manga);

        ModelAndView view = new ModelAndView("title");
        view.addObject("manga", manga);
        view.addObject("chapters", chapters);
        view.addObject("firstChapter", chapters.isEmpty() ? null : chapters.get(0));
        view.addObject("latestChapter", chapters.isEmpty() ? null : chapters.get(chapters.size() - 1));
        view.addObject("chaptersLabel", plural(chapters.size(), "глава", "главы", "глав"));
        view.addObject("similar", mangaService.similar(manga, 6));
        view.addObject("nav", "catalog");
        return view;
    }

    @GetMapping("/read/{slug}/{chapter}")
    public String read(@PathVariable String slug, @PathVariable String chapter, Model model) {
        ReaderService.ChapterView view = readerService.open(slug, chapter).orElseThrow(PageController::notFound);
        model.addAttribute("view", view);
        return "reader";
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    /** One of the five best-rated titles, rotating weekly, so the home page does not look frozen. */
    private static Manga featuredOfTheWeek(List<Manga> popular) {
        if (popular.isEmpty()) {
            return null;
        }
        int week = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return popular.get(week % Math.min(5, popular.size()));
    }

    /** Russian plural form: 1 тайтл, 2 тайтла, 5 тайтлов, 21 тайтл. */
    static String plural(int n, String one, String few, String many) {
        int mod100 = n % 100;
        int mod10 = n % 10;
        if (mod100 >= 11 && mod100 <= 14) return many;
        if (mod10 == 1) return one;
        if (mod10 >= 2 && mod10 <= 4) return few;
        return many;
    }

    private static List<Manga> limit(List<Manga> list) {
        return list.size() > SHELF_SIZE ? list.subList(0, SHELF_SIZE) : list;
    }
}
