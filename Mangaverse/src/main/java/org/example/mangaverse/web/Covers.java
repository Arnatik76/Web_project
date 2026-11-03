package org.example.mangaverse.web;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

/**
 * Responsive cover images for templates ({@code @covers}). Variants are produced by
 * {@code scripts/optimize_covers.py} as {@code /images/variants/<name>-<width>.<avif|webp>}.
 * Covers without variants (e.g. external URLs) simply fall back to the original image.
 */
@Component("covers")
public class Covers {

    private static final Pattern LOCAL = Pattern.compile("^/images/([\\w-]+)\\.(?:jpe?g|png)$");
    private static final List<Integer> WIDTHS = List.of(192, 375);

    private final ResourceUrlProvider urls;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public Covers(ResourceUrlProvider urls) {
        this.urls = urls;
    }

    /** {@code srcset} for the given format ("avif" or "webp"), or {@code null} when no variants exist. */
    public String srcset(String imageUrl, String format) {
        List<String> entries = variants(imageUrl, format);
        return entries.isEmpty() ? null : String.join(", ", entries);
    }

    /** The smallest variant, for places where the image is tiny or blurred anyway. */
    public String thumbnail(String imageUrl) {
        List<String> webp = variants(imageUrl, "webp");
        return webp.isEmpty() ? imageUrl : webp.get(0).substring(0, webp.get(0).indexOf(' '));
    }

    private List<String> variants(String imageUrl, String format) {
        if (imageUrl == null) {
            return List.of();
        }
        return cache.computeIfAbsent(imageUrl + "|" + format, key -> {
            Matcher match = LOCAL.matcher(imageUrl);
            if (!match.matches()) {
                return List.of();
            }
            return WIDTHS.stream()
                    .map(width -> "/images/variants/" + match.group(1) + "-" + width + "." + format)
                    .filter(path -> new ClassPathResource("static" + path).exists())
                    .map(path -> versioned(path) + " " + path.replaceAll(".*-(\\d+)\\..*", "$1") + "w")
                    .toList();
        });
    }

    private String versioned(String path) {
        String url = urls.getForLookupPath(path);
        return url != null ? url : path;
    }
}
