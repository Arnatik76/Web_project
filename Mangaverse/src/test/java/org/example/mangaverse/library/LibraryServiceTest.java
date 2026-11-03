package org.example.mangaverse.library;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.example.mangaverse.web.Covers;
import org.junit.jupiter.api.Test;

class LibraryServiceTest {

    private final LibraryRepository repository = mock(LibraryRepository.class);
    private final Covers covers = mock(Covers.class);
    private final Clock clock = Clock.fixed(Instant.ofEpochMilli(10_000), ZoneOffset.UTC);
    private final LibraryService service = new LibraryService(repository, covers, clock);

    @Test
    void futureTimestampsAreClampedToNow() {
        service.sync(1, new Sync.Request(
                List.of(new LibraryItem("a", LibraryStatus.PLANNED, 99_999_999, null, null)),
                List.of(new ProgressItem("a", "1", 1, 2, false, 99_999_999, null, null))));
        verify(repository).upsertStatus(1, "a", LibraryStatus.PLANNED, 10_000);
        verify(repository).upsertProgress(eq(1L), any(), eq(10_000L));
    }

    @Test
    void unknownTitlesAreNotWritten() {
        when(repository.mangaExists("ghost")).thenReturn(false);
        assertFalse(service.setStatus(1, "ghost", LibraryStatus.READING, 5));
        verify(repository, never()).upsertStatus(anyLong(), anyString(), any(), anyLong());
    }
}
