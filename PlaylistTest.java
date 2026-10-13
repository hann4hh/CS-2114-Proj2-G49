import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests Playlist: constructor, addSong, removeSong, containsSong, songCount. */
class PlaylistTest {
    private static final Song IMAGINE = new Song("Imagine", "John Lennon", 183);
    private static final Song YESTERDAY = new Song("Yesterday", "The Beatles", 125);

    @Test void constructorStoresNameAndStartsEmpty() {
        Playlist p = new Playlist("Workout");
        assertEquals("Workout", p.getName());
        assertEquals(0, p.songCount());
    }

    @Test void nullOrBlankNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Playlist(null));
        assertThrows(IllegalArgumentException.class, () -> new Playlist("  "));
        assertThrows(IllegalArgumentException.class, () -> new Playlist(""));
    }

    @Test void addSongIncreasesCount() {
        Playlist p = new Playlist("Workout");
        p.addSong(IMAGINE);
        assertEquals(1, p.songCount());
        assertTrue(p.containsSong(IMAGINE));
    }

    @Test void addingSameSongTwiceKeepsBothCopies() {
        Playlist p = new Playlist("Workout");
        p.addSong(IMAGINE);
        p.addSong(IMAGINE);
        assertEquals(2, p.songCount());
    }

    @Test void addNullSongIsRejected() {
        Playlist p = new Playlist("Workout");
        assertThrows(IllegalArgumentException.class, () -> p.addSong(null));
        assertEquals(0, p.songCount());
    }

    @Test void removeSongPresentReturnsTrueAndCountDrops() {
        Playlist p = new Playlist("Workout");
        p.addSong(IMAGINE);
        assertTrue(p.removeSong(IMAGINE));
        assertEquals(0, p.songCount());
    }

    @Test void removeSongAbsentOrFromEmptyReturnsFalse() {
        Playlist p = new Playlist("Workout");
        assertFalse(p.removeSong(IMAGINE));          // empty playlist
        p.addSong(YESTERDAY);
        assertFalse(p.removeSong(IMAGINE));          // not in playlist
        assertEquals(1, p.songCount());
    }

    @Test void containsSongTrueAfterAdd() {
        Playlist p = new Playlist("Workout");
        p.addSong(IMAGINE);
        assertTrue(p.containsSong(IMAGINE));
    }

    @Test void containsSongFalseWhenNeverAddedOrEmpty() {
        Playlist p = new Playlist("Workout");
        assertFalse(p.containsSong(IMAGINE));        // empty
        p.addSong(YESTERDAY);
        assertFalse(p.containsSong(IMAGINE));        // never added
    }

    @Test void songCountCountsDuplicates() {
        Playlist p = new Playlist("Workout");
        p.addSong(IMAGINE);
        p.addSong(IMAGINE);
        p.addSong(YESTERDAY);
        assertEquals(3, p.songCount());
    }

    @Test void songCountOfNewPlaylistIsZero() {
        assertEquals(0, new Playlist("Chill").songCount());
    }

    @Test void twoPlaylistsMayShareAName() {
        Playlist a = new Playlist("Workout");
        Playlist b = new Playlist("Workout");
        assertNotEquals(a, b);
    }

    @Test void songsInOrderListsOldestFirstAndKeepsDuplicates() {
        Playlist p = new Playlist("Workout");
        p.addSong(IMAGINE);
        p.addSong(YESTERDAY);
        p.addSong(IMAGINE);
        assertArrayEquals(new Song[] {IMAGINE, YESTERDAY, IMAGINE}, p.songsInOrder());
    }

    @Test void songsInOrderOfEmptyPlaylistIsEmpty() {
        assertEquals(0, new Playlist("Chill").songsInOrder().length);
    }
}
