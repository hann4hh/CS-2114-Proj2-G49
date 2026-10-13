import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

/** Tests every public MelodyApp method with scripted input and captured output. */
class MelodyAppTest {
    private ByteArrayOutputStream bytes;

    /** Builds an app reading scripted input; output is captured in `bytes`. */
    private MelodyApp app(String input) {
        bytes = new ByteArrayOutputStream();
        return new MelodyApp(new Scanner(input),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), new Random(1));
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private MelodyApp appWithSongs(String... titles) {
        MelodyApp app = app("");
        for (int i = 0; i < titles.length; i++) {
            assertTrue(app.addSong(titles[i], "Artist " + i, String.valueOf(100 + i)));
        }
        bytes.reset();   // discard the "Added:" lines so later asserts see only the method under test
        return app;
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            n++;
        }
        return n;
    }

    // ---- constructor
    @Test void constructorStartsWithEmptyLibrary() {
        MelodyApp app = app("");
        app.showCount();
        assertTrue(output().contains("Song count: 0"));
    }

    @Test void nullScannerIsRejectedImmediately() {
        assertThrows(IllegalArgumentException.class, () -> new MelodyApp(null));
        assertThrows(IllegalArgumentException.class,
                () -> new MelodyApp(null, System.out, new Random()));
        assertThrows(IllegalArgumentException.class,
                () -> new MelodyApp(new Scanner(""), null, new Random()));
        assertThrows(IllegalArgumentException.class,
                () -> new MelodyApp(new Scanner(""), System.out, null));
    }

    // ---- addSong (interactive)
    @Test void addSongInteractiveAddsValidSong() {
        MelodyApp app = app("Imagine\nJohn Lennon\n183\n");
        app.addSong();
        app.listSongs();
        assertTrue(output().contains("1. Imagine - John Lennon (3:03)"));
    }

    @Test void addSongRePromptsOnNegativeDurationInsteadOfCrashing() {
        MelodyApp app = app("Imagine\nJohn Lennon\n-5\nImagine\nJohn Lennon\n183\n");
        app.addSong();
        app.showCount();
        assertTrue(output().contains("Duration must be greater than 0"));
        assertEquals(2, count(output(), "Title: "));   // prompted again
        assertTrue(output().contains("Song count: 1"));
    }

    @Test void addSongRePromptsOnNonNumericDuration() {
        MelodyApp app = app("Imagine\nJohn Lennon\nabc\nImagine\nJohn Lennon\n183\n");
        app.addSong();
        app.showCount();
        assertTrue(output().contains("whole number of seconds"));
        assertTrue(output().contains("Song count: 1"));
    }

    @Test void addSongRePromptsOnBlankTitleAndArtist() {
        MelodyApp app = app("\nJohn\n100\nImagine\n   \n100\nImagine\nJohn Lennon\n183\n");
        app.addSong();
        app.showCount();
        assertTrue(output().contains("Title cannot be blank"));
        assertTrue(output().contains("Artist cannot be blank"));
        assertTrue(output().contains("Song count: 1"));
    }

    @Test void addSongStopsCleanlyWhenInputEndsMidEntry() {
        MelodyApp app = app("Imagine\n");   // artist and duration never arrive
        app.addSong();
        app.showCount();
        assertTrue(output().contains("Input ended"));
        assertTrue(output().contains("Song count: 0"));
    }

    // ---- addSong (single attempt)
    @Test void addSongFromTextReturnsTrueForValidInput() {
        MelodyApp app = app("");
        assertTrue(app.addSong("Imagine", "John Lennon", " 183 "));
        assertTrue(output().contains("Added: Imagine - John Lennon (3:03)"));
    }

    @Test void addSongFromTextRejectsEveryBadCaseWithoutThrowing() {
        MelodyApp app = app("");
        assertFalse(app.addSong("", "A", "100"));
        assertFalse(app.addSong(null, "A", "100"));
        assertFalse(app.addSong("T", "", "100"));
        assertFalse(app.addSong("T", null, "100"));
        assertFalse(app.addSong("T", "A", "-1"));
        assertFalse(app.addSong("T", "A", "0"));
        assertFalse(app.addSong("T", "A", "abc"));
        assertFalse(app.addSong("T", "A", "12.5"));
        assertFalse(app.addSong("T", "A", ""));
        assertFalse(app.addSong("T", "A", null));
        assertFalse(app.addSong("T", "A", "99999999999999999999"));   // overflows int
        app.showCount();
        assertTrue(output().contains("Song count: 0"));
    }

    @Test void duplicateSongsAreAllowed() {
        MelodyApp app = app("");
        assertTrue(app.addSong("Imagine", "John Lennon", "183"));
        assertTrue(app.addSong("Imagine", "John Lennon", "183"));
        bytes.reset();
        app.showCount();
        assertTrue(output().contains("Song count: 2"));
    }

    // ---- removeSong
    @Test void removeSongExistingTitleRemovesOneCopyAndCountDrops() {
        MelodyApp app = appWithSongs("Imagine", "Yesterday");
        app.removeSong("imagine");   // case-insensitive
        app.showCount();
        assertTrue(output().contains("Removed: Imagine"));
        assertTrue(output().contains("Song count: 1"));
    }

    @Test void removeSongMissingTitleSaysNotFoundWithoutCrashing() {
        MelodyApp app = appWithSongs("Imagine");
        app.removeSong("Nope");
        app.showCount();
        assertTrue(output().contains("Not found: Nope"));
        assertTrue(output().contains("Song count: 1"));
    }

    @Test void removeSongFromEmptyLibraryAndBlankTitleAreSafe() {
        MelodyApp app = app("");
        app.removeSong("Imagine");
        app.removeSong("  ");
        app.removeSong(null);
        assertTrue(output().contains("Not found: Imagine"));
        assertEquals(2, count(output(), "Please enter a title."));
    }

    @Test void removeSongTakesOnlyOneOfTwoIdenticalCopies() {
        MelodyApp app = app("");
        app.addSong("Imagine", "John Lennon", "183");
        app.addSong("Imagine", "John Lennon", "183");
        bytes.reset();
        app.removeSong("Imagine");
        app.showCount();
        assertTrue(output().contains("Song count: 1"));
    }

    @Test void removeSongRemovesTheMostRecentCopyAndKeepsOtherSongsInOrder() {
        MelodyApp app = app("");
        app.addSong("Imagine", "John Lennon", "183");
        app.addSong("Yesterday", "The Beatles", "125");
        app.addSong("Imagine", "John Lennon", "183");
        bytes.reset();
        app.removeSong("Imagine");
        app.listSongs();
        String out = output();
        assertTrue(out.contains("1. Imagine - John Lennon"));
        assertTrue(out.contains("2. Yesterday - The Beatles"));
        assertFalse(out.contains("3."));
    }

    @Test void removeSongOfTwoDifferentSongsSharingATitleRemovesTheOldestMatch() {
        MelodyApp app = app("");
        app.addSong("Imagine", "John Lennon", "183");
        app.addSong("Imagine", "A Perfect Circle", "180");
        bytes.reset();
        app.removeSong("Imagine");
        bytes.reset();   // drop the "Removed: ..." message, which names the removed song
        app.listSongs();
        assertFalse(output().contains("John Lennon"));
        assertTrue(output().contains("A Perfect Circle"));
    }

    @Test void thousandsOfDuplicatesAreCountedAndRemovedCorrectly() {   // adversarial volume case
        MelodyApp app = app("");
        for (int i = 0; i < 3000; i++) {
            app.addSong("Loop", "Band", "60");
        }
        app.addSong("Other", "Band", "60");
        bytes.reset();
        app.showCount();
        assertTrue(output().contains("Song count: 3001"));
        bytes.reset();
        app.removeAllCopies("Loop");
        app.showCount();
        assertTrue(output().contains("Removed 3000 copies of: Loop"));
        assertTrue(output().contains("Song count: 1"));
    }

    // ---- searchSong
    @Test void searchSongPrintsMatches() {
        MelodyApp app = appWithSongs("Imagine", "Yesterday", "Imagine Dragons");
        app.searchSong("IMAGINE");
        assertTrue(output().contains("Imagine - Artist 0"));
        assertTrue(output().contains("Imagine Dragons - Artist 2"));
        assertFalse(output().contains("Yesterday"));
    }

    @Test void searchSongWithNoMatchesPrintsNoResults() {
        MelodyApp app = appWithSongs("Imagine");
        app.searchSong("zzz");
        assertTrue(output().contains("No results for: zzz"));
    }

    @Test void searchSongBlankOrNullAndEmptyLibraryAreSafe() {
        MelodyApp app = app("");
        app.searchSong("");
        app.searchSong(null);
        app.searchSong("anything");   // empty library
        assertEquals(2, count(output(), "Please enter a title to search for."));
        assertTrue(output().contains("No results for: anything"));
    }

    // ---- listSongs
    @Test void listSongsPrintsAllInInsertionOrder() {
        MelodyApp app = appWithSongs("Alpha", "Beta", "Gamma");
        app.listSongs();
        String out = output();
        assertTrue(out.contains("Songs (3):"));
        int a = out.indexOf("1. Alpha");
        int b = out.indexOf("2. Beta");
        int c = out.indexOf("3. Gamma");
        assertTrue(a >= 0 && a < b && b < c);
    }

    @Test void listSongsOnEmptyLibraryPrintsEmptyMessage() {
        MelodyApp app = app("");
        app.listSongs();
        assertTrue(output().contains("The playlist is empty."));
    }

    // ---- showCount
    @Test void showCountPrintsThreeAfterThreeAdds() {
        MelodyApp app = appWithSongs("A", "B", "C");
        app.showCount();
        assertTrue(output().contains("Song count: 3"));
    }

    @Test void showCountOnEmptyLibraryPrintsZero() {
        MelodyApp app = app("");
        app.showCount();
        assertTrue(output().contains("Song count: 0"));
    }

    // ---- shufflePlay (stretch)
    @Test void shuffleOrderHasEachSongOnceAndLibraryIsUnchanged() {
        MelodyApp app = appWithSongs("A", "B", "C", "D");
        app.listSongs();
        String before = output();
        List<Song> order = app.buildShuffleOrder();
        assertEquals(4, order.size());
        List<String> titles = new ArrayList<>();
        for (Song s : order) {
            titles.add(s.getTitle());
        }
        assertTrue(titles.containsAll(List.of("A", "B", "C", "D")));
        bytes.reset();
        app.listSongs();
        assertEquals(before, output());   // library order and contents untouched
    }

    @Test void shufflePlayPlaysEachSongExactlyOnce() {
        MelodyApp app = appWithSongs("A", "B", "C", "D");
        app.shufflePlay();
        assertEquals(4, count(output(), "Now playing: "));
        for (String t : new String[] {"A - ", "B - ", "C - ", "D - "}) {
            assertEquals(1, count(output(), "Now playing: " + t));
        }
    }

    @Test void shuffleActuallyReordersALargeLibrary() {
        MelodyApp app = app("");
        List<String> inserted = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            app.addSong("Song" + i, "Artist", "100");
            inserted.add("Song" + i);
        }
        List<String> shuffled = new ArrayList<>();
        for (Song s : app.buildShuffleOrder()) {
            shuffled.add(s.getTitle());
        }
        assertNotEquals(inserted, shuffled);   // seeded Random(1): deterministic
        assertEquals(30, shuffled.size());
    }

    @Test void shuffleOfEmptyLibraryPrintsNothingToPlay() {
        MelodyApp app = app("");
        app.shufflePlay();
        assertTrue(output().contains("Nothing to play."));
        assertTrue(app.buildShuffleOrder().isEmpty());
    }

    @Test void shuffleOfOneSongPlaysThatSong() {
        MelodyApp app = appWithSongs("Solo");
        app.shufflePlay();
        assertEquals(1, count(output(), "Now playing: Solo - "));
    }

    // ---- showHistory (stretch; LinkedChain<String>)
    @Test void historyListsPlayedTitlesAfterShuffle() {
        MelodyApp app = appWithSongs("A", "B");
        app.shufflePlay();
        bytes.reset();
        app.showHistory();
        assertTrue(output().contains("1. "));
        assertTrue(output().contains("2. "));
    }

    @Test void historyKeepsPlayOrderOldestFirst() {
        MelodyApp app = appWithSongs("Solo");
        app.shufflePlay();
        app.shufflePlay();
        bytes.reset();
        app.showHistory();
        assertTrue(output().contains("1. Solo"));
        assertTrue(output().contains("2. Solo"));
        assertFalse(output().contains("3."));
    }

    @Test void historyIsEmptyBeforeAnythingIsPlayed() {
        MelodyApp app = app("");
        app.showHistory();
        assertTrue(output().contains("No songs played yet."));
    }

    // ---- removeAllCopies (stretch)
    @Test void removeAllCopiesRemovesEveryDuplicate() {
        MelodyApp app = app("");
        app.addSong("Imagine", "John Lennon", "183");
        app.addSong("Imagine", "John Lennon", "183");
        app.addSong("Imagine", "John Lennon", "183");
        app.addSong("Yesterday", "The Beatles", "125");
        bytes.reset();
        app.removeAllCopies("Imagine");
        app.showCount();
        assertTrue(output().contains("Removed 3 copies of: Imagine"));
        assertTrue(output().contains("Song count: 1"));
    }

    @Test void removeAllCopiesMissingOrBlankIsSafe() {
        MelodyApp app = app("");
        app.removeAllCopies("Nope");
        app.removeAllCopies(" ");
        assertTrue(output().contains("Not found: Nope"));
        assertTrue(output().contains("Please enter a title."));
    }

    // ---- playlists (stretch; LinkedChain<Playlist> holding LinkedChain<Song>)
    @Test void createPlaylistAndAddSongThenList() {
        MelodyApp app = appWithSongs("Imagine");
        assertTrue(app.createPlaylist("Workout"));
        assertTrue(app.addSongToPlaylist("workout", "imagine"));
        bytes.reset();
        app.listPlaylists();
        assertTrue(output().contains("Workout (1 songs)"));
        assertTrue(output().contains("Imagine - Artist 0"));
    }

    @Test void createPlaylistRejectsBlankNameAndAllowsDuplicateNames() {
        MelodyApp app = app("");
        assertFalse(app.createPlaylist(""));
        assertFalse(app.createPlaylist(null));
        assertTrue(app.createPlaylist("Workout"));
        assertTrue(app.createPlaylist("Workout"));
        bytes.reset();
        app.listPlaylists();
        assertEquals(2, count(output(), "Workout (0 songs)"));
    }

    @Test void addSongToPlaylistHandlesMissingPlaylistSongAndBlanks() {
        MelodyApp app = appWithSongs("Imagine");
        app.createPlaylist("Workout");
        assertFalse(app.addSongToPlaylist("Chill", "Imagine"));
        assertFalse(app.addSongToPlaylist("Workout", "Nope"));
        assertFalse(app.addSongToPlaylist("", "Imagine"));
        assertFalse(app.addSongToPlaylist("Workout", null));
        assertTrue(output().contains("Playlist not found: Chill"));
        assertTrue(output().contains("Not found: Nope"));
    }

    @Test void playlistsAndTheirSongsListOldestFirst() {
        MelodyApp app = app("");
        app.addSong("A", "x", "10");
        app.addSong("B", "x", "10");
        app.createPlaylist("First");
        app.createPlaylist("Second");
        app.addSongToPlaylist("First", "A");
        app.addSongToPlaylist("First", "B");
        bytes.reset();
        app.listPlaylists();
        String out = output();
        assertTrue(out.indexOf("First (2 songs)") < out.indexOf("Second (0 songs)"));
        assertTrue(out.indexOf("A - x") < out.indexOf("B - x"));
    }

    @Test void listPlaylistsWhenNoneExist() {
        MelodyApp app = app("");
        app.listPlaylists();
        assertTrue(output().contains("No playlists yet."));
    }

    // ---- run (menu loop)
    @Test void menuSessionAddsListsCountsAndExits() {
        MelodyApp app = app("1\nImagine\nJohn Lennon\n183\n5\n6\n0\n");
        app.run();
        assertTrue(output().contains("Added: Imagine - John Lennon (3:03)"));
        assertTrue(output().contains("Song count: 1"));
        assertTrue(output().contains("Goodbye."));
    }

    @Test void menuSurvivesInvalidChoicesAndEndOfInput() {
        MelodyApp app = app("banana\n99\n\n6\n");   // no exit option, input just ends
        app.run();
        assertEquals(3, count(output(), "Invalid choice"));
        assertTrue(output().contains("Song count: 0"));
        assertTrue(output().contains("Input ended. Goodbye."));
    }

    @Test void menuRoutesRemoveSearchShuffleAndPlaylistChoices() {
        String script = "1\nImagine\nJohn\n183\n"   // add
                + "4\nimag\n"                       // search
                + "7\n8\n"                          // shuffle + history
                + "9\nWorkout\n10\nWorkout\nImagine\n11\n"
                + "2\nImagine\n3\nImagine\n0\n";
        MelodyApp app = app(script);
        app.run();
        String out = output();
        assertTrue(out.contains("Now playing: Imagine"));
        assertTrue(out.contains("Created playlist: Workout"));
        assertTrue(out.contains("Added Imagine to Workout"));
        assertTrue(out.contains("Removed: Imagine"));
        assertTrue(out.contains("Not found: Imagine"));   // option 3 after option 2 already removed it
    }
}
