package org.example.mangaverse.library;

public enum LibraryStatus {
    READING("Читаю"),
    PLANNED("В планах"),
    COMPLETED("Прочитано"),
    ON_HOLD("Отложено"),
    DROPPED("Брошено");

    private final String label;

    LibraryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
