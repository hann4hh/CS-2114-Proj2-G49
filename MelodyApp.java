import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Controller: reads menu input, validates it, calls LinkedChain/Playlist methods, prints results.
 * It stores nothing itself: all storage goes through three LinkedChain instances of different
 * element types (Song, Playlist, String) and is touched only via the chain's public methods
 * (add, remove, count, contains, size, isEmpty, toArray). add() stores newest-first, so reads go
 * through ChainView, which hands back a snapshot array in insertion order.
 */
public class MelodyApp {
    private LinkedChain<Song> library = new LinkedChain<>();
    private LinkedChain<Playlist> playlists = new LinkedChain<>();   // stretch: playlists
    private LinkedChain<String> playHistory = new LinkedChain<>();   // titles played by shuffle
    private Scanner scanner;
    private PrintStream out;
    private Random random;

    /** Console app: output goes to System.out. Throws IllegalArgumentException if scanner is null. O(1). */
    public MelodyApp(Scanner scanner) {
        this(scanner, System.out, new Random());
    }

    /** Testable form: inject output stream and random source. Any null argument is rejected. O(1). */
    public MelodyApp(Scanner scanner, PrintStream out, Random random) {
        if (scanner == null || out == null || random == null) {
            throw new IllegalArgumentException("scanner, out, and random must not be null");
        }
        this.scanner = scanner;
        this.out = out;
        this.random = random;
    }

    // ---------------------------------------------------------------- MVP

    /** Interactive add: prompts for title, artist, duration; on bad input explains and re-prompts. O(1). */
    public void addSong() {
        while (true) {
            String title = readLine("Title: ");
            String artist = title == null ? null : readLine("Artist: ");
            String duration = artist == null ? null : readLine("Duration (seconds): ");
            if (duration == null) {
                out.println("Input ended; no song added.");
                return;
            }
            if (addSong(title, artist, duration)) {
                return;
            }
            out.println("Please try again.");
        }
    }

    /**
     * One validated add attempt from raw text. Returns true if added. Never throws on bad input:
     * a non-numeric duration is caught here, everything else by the Song constructor. O(1).
     */
    public boolean addSong(String title, String artist, String durationText) {
        if (durationText == null) {
            out.println("Invalid input: duration is required.");
            return false;
        }
        int duration;
        try {
            duration = Integer.parseInt(durationText.trim());
        } catch (NumberFormatException e) {
            out.println("Invalid input: duration must be a whole number of seconds.");
            return false;
        }
        try {
            Song song = new Song(title, artist, duration);
            library.add(song);
            out.println("Added: " + song);
            return true;
        } catch (IllegalArgumentException e) {
            out.println("Invalid input: " + e.getMessage() + ".");
            return false;
        }
    }

    /** Removes ONE copy of the first song whose title matches (ignoring case). O(n). */
    public void removeSong(String title) {
        if (isBlank(title)) {
            out.println("Please enter a title.");
            return;
        }
        Song found = findByTitle(title);
        if (found == null) {
            out.println("Not found: " + title.trim());
            return;
        }
        library.remove(found);   // removes the most recently added copy equal to found
        out.println("Removed: " + found);
    }

    /** Prints songs whose title contains the text (ignoring case), or a "no results" message. O(n). */
    public void searchSong(String title) {
        if (isBlank(title)) {
            out.println("Please enter a title to search for.");
            return;
        }
        String query = title.trim().toLowerCase();
        int matches = 0;
        for (Song s : songs()) {
            if (s.getTitle().toLowerCase().contains(query)) {
                out.println("  " + s);
                matches++;
            }
        }
        if (matches == 0) {
            out.println("No results for: " + title.trim());
        }
    }

    /** Prints every song in insertion order, or an empty-playlist message. O(n). */
    public void listSongs() {
        if (library.isEmpty()) {
            out.println("The playlist is empty.");
            return;
        }
        out.println("Songs (" + library.size() + "):");
        int i = 1;
        for (Song s : songs()) {
            out.println("  " + i++ + ". " + s);
        }
    }

    /** Prints the number of songs (duplicates counted). O(1). */
    public void showCount() {
        out.println("Song count: " + library.size());
    }

    // ---------------------------------------------------------------- Stretch: shuffle, duplicates, playlists

    /** Returns a randomized copy of the library order; the library itself is never changed. O(n). */
    public List<Song> buildShuffleOrder() {
        List<Song> order = new ArrayList<>(Arrays.asList(songs()));   // copy; library untouched
        Collections.shuffle(order, random);
        return order;
    }

    /** Plays the library in a random order (each song once) and records titles in the history chain. O(n). */
    public void shufflePlay() {
        List<Song> order = buildShuffleOrder();
        if (order.isEmpty()) {
            out.println("Nothing to play.");
            return;
        }
        for (Song s : order) {
            out.println("Now playing: " + s);
            playHistory.add(s.getTitle());
        }
    }

    /** Prints titles played so far, oldest first. O(n). */
    public void showHistory() {
        if (playHistory.isEmpty()) {
            out.println("No songs played yet.");
            return;
        }
        int i = 1;
        for (String title : ChainView.inInsertionOrder(playHistory, new String[0])) {
            out.println("  " + i++ + ". " + title);
        }
    }

    /** Removes EVERY copy of the first song whose title matches (ignoring case). O(n). */
    public void removeAllCopies(String title) {
        if (isBlank(title)) {
            out.println("Please enter a title.");
            return;
        }
        Song found = findByTitle(title);
        if (found == null) {
            out.println("Not found: " + title.trim());
            return;
        }
        int copies = library.count(found);   // the chain has no removeAll, so count then remove each
        for (int i = 0; i < copies; i++) {
            library.remove(found);
        }
        out.println("Removed " + copies + " cop" + (copies == 1 ? "y" : "ies") + " of: " + found);
    }

    /** Creates an empty playlist; duplicate names are allowed. Returns false on a blank name. O(1). */
    public boolean createPlaylist(String name) {
        try {
            Playlist p = new Playlist(name);
            playlists.add(p);
            out.println("Created playlist: " + p.getName());
            return true;
        } catch (IllegalArgumentException e) {
            out.println("Invalid input: " + e.getMessage() + ".");
            return false;
        }
    }

    /** Adds the first library song with this title to the first playlist with this name. O(p + n). */
    public boolean addSongToPlaylist(String playlistName, String songTitle) {
        if (isBlank(playlistName) || isBlank(songTitle)) {
            out.println("Please enter both a playlist name and a song title.");
            return false;
        }
        Playlist playlist = findPlaylist(playlistName);
        if (playlist == null) {
            out.println("Playlist not found: " + playlistName.trim());
            return false;
        }
        Song song = findByTitle(songTitle);
        if (song == null) {
            out.println("Not found: " + songTitle.trim());
            return false;
        }
        playlist.addSong(song);
        out.println("Added " + song.getTitle() + " to " + playlist.getName());
        return true;
    }

    /** Prints each playlist and its songs. O(total songs across playlists). */
    public void listPlaylists() {
        if (playlists.isEmpty()) {
            out.println("No playlists yet.");
            return;
        }
        for (Playlist p : ChainView.inInsertionOrder(playlists, new Playlist[0])) {
            out.println(p);
            for (Song s : p.songsInOrder()) {
                out.println("    " + s);
            }
        }
    }

    // ---------------------------------------------------------------- Menu loop

    /** Runs the text menu until the user exits or input ends. */
    public void run() {
        while (true) {
            printMenu();
            String choice = readLine("Choose an option: ");
            if (choice == null) {
                out.println("Input ended. Goodbye.");
                return;
            }
            if (!handle(choice.trim())) {
                out.println("Goodbye.");
                return;
            }
        }
    }

    /** Dispatches one menu choice; returns false when the user chose to exit. */
    private boolean handle(String choice) {
        switch (choice) {
            case "1": addSong(); break;
            case "2": removeSong(readLine("Title to remove: ")); break;
            case "3": removeAllCopies(readLine("Title to remove all copies of: ")); break;
            case "4": searchSong(readLine("Title to search for: ")); break;
            case "5": listSongs(); break;
            case "6": showCount(); break;
            case "7": shufflePlay(); break;
            case "8": showHistory(); break;
            case "9": createPlaylist(readLine("Playlist name: ")); break;
            case "10":
                String playlistName = readLine("Playlist name: ");
                String songTitle = playlistName == null ? null : readLine("Song title: ");
                addSongToPlaylist(playlistName, songTitle);
                break;
            case "11": listPlaylists(); break;
            case "0": return false;
            default: out.println("Invalid choice: please enter a number from the menu.");
        }
        return true;
    }

    private void printMenu() {
        out.println();
        out.println("=== Melody ===");
        out.println(" 1. Add song            2. Remove one copy    3. Remove all copies");
        out.println(" 4. Search by title     5. List songs         6. Show count");
        out.println(" 7. Shuffle play        8. Play history       9. Create playlist");
        out.println("10. Add song to playlist  11. List playlists    0. Exit");
    }

    // ---------------------------------------------------------------- Helpers

    /** Prompts and reads one line; returns null when input has ended. */
    private String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    /** Library snapshot, oldest song first. */
    private Song[] songs() {
        return ChainView.inInsertionOrder(library, new Song[0]);
    }

    /** First (oldest) song whose title equals the text ignoring case, or null. */
    private Song findByTitle(String title) {
        String wanted = title.trim();
        for (Song s : songs()) {
            if (s.getTitle().equalsIgnoreCase(wanted)) {
                return s;
            }
        }
        return null;
    }

    /** First playlist whose name equals the text ignoring case, or null. */
    private Playlist findPlaylist(String name) {
        String wanted = name.trim();
        for (Playlist p : ChainView.inInsertionOrder(playlists, new Playlist[0])) {
            if (p.getName().equalsIgnoreCase(wanted)) {
                return p;
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
