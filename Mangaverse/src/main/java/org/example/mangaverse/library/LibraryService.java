package org.example.mangaverse.library;

import java.time.Clock;
import java.util.List;

import org.example.mangaverse.web.Covers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {

    private final LibraryRepository repository;
    private final Covers covers;
    private final Clock clock;

    public LibraryService(LibraryRepository repository, Covers covers, Clock clock) {
        this.repository = repository;
        this.covers = covers;
        this.clock = clock;
    }

    /** Merges everything a device has into the account and returns the account's full state. */
    @Transactional
    public Sync.State sync(long userId, Sync.Request request) {
        request.library().forEach(item -> repository.upsertStatus(userId, item.slug(), item.status(), clamp(item.updatedAt())));
        request.progress().forEach(item -> repository.upsertProgress(userId, item, clamp(item.updatedAt())));
        return state(userId);
    }

    public Sync.State state(long userId) {
        List<LibraryItem> library = repository.findLibrary(userId).stream()
                .map(i -> new LibraryItem(i.slug(), i.status(), i.updatedAt(), i.title(), covers.thumbnail(i.cover())))
                .toList();
        List<ProgressItem> progress = repository.findProgress(userId).stream()
                .map(p -> new ProgressItem(p.slug(), p.chapter(), p.page(), p.total(), p.finished(), p.updatedAt(),
                        p.title(), covers.thumbnail(p.cover())))
                .toList();
        return new Sync.State(library, progress);
    }

    /** @return false when the title does not exist */
    public boolean setStatus(long userId, String slug, LibraryStatus status, long updatedAt) {
        if (!repository.mangaExists(slug)) {
            return false;
        }
        repository.upsertStatus(userId, slug, status, clamp(updatedAt));
        return true;
    }

    /** @return false when the title does not exist */
    public boolean saveProgress(long userId, ProgressItem item) {
        if (!repository.mangaExists(item.slug())) {
            return false;
        }
        repository.upsertProgress(userId, item, clamp(item.updatedAt()));
        return true;
    }

    /** A device with a wrong clock must not be able to "win" every future sync. */
    private long clamp(long updatedAt) {
        return Math.min(Math.max(updatedAt, 0), clock.millis());
    }
}
