package org.example.mangaverse.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;

public record Chapter(
        @Id Long id,
        Long mangaId,
        BigDecimal number,
        String title,
        LocalDate publishedOn
) {
    /** Chapter number as people write it: 12, 12.5 — never 12.0 or 1E+1. */
    public String numberLabel() {
        return number.stripTrailingZeros().toPlainString();
    }

    public String label() {
        return "Глава " + numberLabel();
    }
}
