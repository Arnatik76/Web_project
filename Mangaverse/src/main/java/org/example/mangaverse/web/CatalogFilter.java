package org.example.mangaverse.web;

import org.example.mangaverse.models.MangaType;
import org.example.mangaverse.services.CatalogSort;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Current catalog filters. Templates use the {@code with*} methods to build links that change one filter
 * and keep the others, so filtered views are plain, shareable URLs that work without JavaScript.
 */
public record CatalogFilter(String q, String genre, MangaType type, CatalogSort sort) {

    public CatalogFilter {
        q = blankToNull(q);
        genre = blankToNull(genre);
        sort = sort == null ? CatalogSort.POPULAR : sort;
    }

    public String getQ() {
        return q;
    }

    public String getGenre() {
        return genre;
    }

    public MangaType getType() {
        return type;
    }

    public CatalogSort getSort() {
        return sort;
    }

    public boolean isFiltered() {
        return q != null || genre != null || type != null;
    }

    public CatalogFilter withGenre(String value) {
        return new CatalogFilter(q, value, type, sort);
    }

    public CatalogFilter withType(MangaType value) {
        return new CatalogFilter(q, genre, value, sort);
    }

    public CatalogFilter withSort(CatalogSort value) {
        return new CatalogFilter(q, genre, type, value);
    }

    public CatalogFilter withoutQuery() {
        return new CatalogFilter(null, genre, type, sort);
    }

    public String getUrl() {
        return pageUrl(1);
    }

    /** URL of the given 1-based results page with the same filters. */
    public String pageUrl(int page) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/catalog");
        if (q != null) uri.queryParam("q", q);
        if (genre != null) uri.queryParam("genre", genre);
        if (type != null) uri.queryParam("type", type.name().toLowerCase());
        if (sort != CatalogSort.POPULAR) uri.queryParam("sort", sort.name().toLowerCase());
        if (page > 1) uri.queryParam("page", page);
        return uri.encode().build().toUriString();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
