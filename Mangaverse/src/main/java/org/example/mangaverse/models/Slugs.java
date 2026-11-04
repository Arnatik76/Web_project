package org.example.mangaverse.models;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

/** URL slugs: "Милый дом" → "milyy-dom", "Omniscient Reader's Viewpoint" → "omniscient-readers-viewpoint". */
public final class Slugs {

    private static final Map<Character, String> CYRILLIC = Map.ofEntries(
            Map.entry('а', "a"), Map.entry('б', "b"), Map.entry('в', "v"), Map.entry('г', "g"), Map.entry('д', "d"),
            Map.entry('е', "e"), Map.entry('ё', "e"), Map.entry('ж', "zh"), Map.entry('з', "z"), Map.entry('и', "i"),
            Map.entry('й', "y"), Map.entry('к', "k"), Map.entry('л', "l"), Map.entry('м', "m"), Map.entry('н', "n"),
            Map.entry('о', "o"), Map.entry('п', "p"), Map.entry('р', "r"), Map.entry('с', "s"), Map.entry('т', "t"),
            Map.entry('у', "u"), Map.entry('ф', "f"), Map.entry('х', "kh"), Map.entry('ц', "ts"), Map.entry('ч', "ch"),
            Map.entry('ш', "sh"), Map.entry('щ', "shch"), Map.entry('ъ', ""), Map.entry('ы', "y"), Map.entry('ь', ""),
            Map.entry('э', "e"), Map.entry('ю', "yu"), Map.entry('я', "ya"));

    private Slugs() {
    }

    public static String of(String text) {
        StringBuilder latin = new StringBuilder();
        for (char c : text.toLowerCase(Locale.ROOT).toCharArray()) {
            latin.append(CYRILLIC.getOrDefault(c, String.valueOf(c)));
        }
        String slug = Normalizer.normalize(latin, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")          // ū → u
                .replaceAll("['’]", "")            // reader's → readers
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (slug.length() > 200) {
            slug = slug.substring(0, 200).replaceAll("-+$", "");
        }
        // Numeric paths are reserved for legacy /title/{id} links.
        if (slug.isEmpty()) {
            return "title";
        }
        return slug.chars().allMatch(Character::isDigit) ? "title-" + slug : slug;
    }
}
