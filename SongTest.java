import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests Song: constructor, getters, equals, hashCode. */
class SongTest {
    @Test void constructorBuildsValidSong() {
        Song s = new Song("Imagine", "John Lennon", 183);
        assertEquals("Imagine", s.getTitle());
    }

    @Test void blankTitleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Song("", "John Lennon", 183));
        assertThrows(IllegalArgumentException.class, () -> new Song("   ", "John Lennon", 183));
    }

    @Test void nullTitleOrArtistIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Song(null, "John Lennon", 183));
        assertThrows(IllegalArgumentException.class, () -> new Song("Imagine", null, 183));
    }

    @Test void blankArtistIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Song("Imagine", " ", 183));
    }

    @Test void zeroOrNegativeDurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Song("Imagine", "John Lennon", 0));
        assertThrows(IllegalArgumentException.class, () -> new Song("Imagine", "John Lennon", -5));
    }

    @Test void gettersReturnConstructorValues() {
        Song s = new Song("Imagine", "John Lennon", 183);
        assertEquals("Imagine", s.getTitle());
        assertEquals("John Lennon", s.getArtist());
        assertEquals(183, s.getDuration());
    }

    @Test void constructorTrimsSurroundingSpaces() {
        Song s = new Song("  Imagine ", " John Lennon  ", 183);
        assertEquals("Imagine", s.getTitle());
        assertEquals("John Lennon", s.getArtist());
        assertEquals(s, new Song("Imagine", "John Lennon", 183));
    }

    @Test void equalsTrueForIdenticalFields() {
        assertEquals(new Song("Imagine", "John Lennon", 183), new Song("Imagine", "John Lennon", 183));
    }

    @Test void equalsFalseWhenAnyFieldDiffers() {
        Song base = new Song("Imagine", "John Lennon", 183);
        assertNotEquals(base, new Song("Imagine", "John Lennon", 184));     // duration
        assertNotEquals(base, new Song("Imagine", "A Perfect Circle", 183)); // cover by another artist
        assertNotEquals(base, new Song("Imagine!", "John Lennon", 183));    // title
    }

    @Test void equalsHandlesOtherTypesAndNullWithoutThrowing() {
        Song s = new Song("Imagine", "John Lennon", 183);
        assertFalse(s.equals("Imagine"));
        assertFalse(s.equals(null));
        assertTrue(s.equals(s));
    }

    @Test void hashCodeMatchesForEqualSongs() {
        assertEquals(new Song("Imagine", "John Lennon", 183).hashCode(),
                new Song("Imagine", "John Lennon", 183).hashCode());
    }

    @Test void hashCodeNeverThrowsForDifferentSongs() {
        Song a = new Song("Imagine", "John Lennon", 183);
        Song b = new Song("Imagine", "John Lennon", 184);
        assertNotEquals(a, b);
        assertDoesNotThrow(() -> { a.hashCode(); b.hashCode(); });
    }

    @Test void toStringShowsMinutesAndSeconds() {
        assertEquals("Imagine - John Lennon (3:03)", new Song("Imagine", "John Lennon", 183).toString());
        assertEquals("Intro - Band (0:05)", new Song("Intro", "Band", 5).toString());
    }
}
