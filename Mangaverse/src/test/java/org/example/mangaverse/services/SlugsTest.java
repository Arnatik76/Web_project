package org.example.mangaverse.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.mangaverse.models.Slugs;
import org.junit.jupiter.api.Test;

class SlugsTest {

    @Test
    void makesReadableSlugs() {
        assertEquals("solo-leveling", Slugs.of("Solo Leveling"));
        assertEquals("omniscient-readers-viewpoint", Slugs.of("Omniscient Reader's Viewpoint"));
        assertEquals("one-punch-man", Slugs.of("One-Punch Man"));
        assertEquals("hunter-x-hunter", Slugs.of("Hunter x Hunter"));
        assertEquals("black-clover-yuki", Slugs.of("Black Clover — Yūki!"));
    }

    @Test
    void transliteratesCyrillic() {
        assertEquals("milyy-dom", Slugs.of("Милый дом"));
        assertEquals("svoloch", Slugs.of("Сволочь"));
        assertEquals("shchuka-i-ezh", Slugs.of("Щука и ёж"));
    }

    @Test
    void neverReturnsEmptyOrNumericSlug() {
        assertEquals("title", Slugs.of("!!!"));
        assertEquals("title-2024", Slugs.of("2024"));
    }
}
