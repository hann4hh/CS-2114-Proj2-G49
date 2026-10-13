/**
 * A named collection of songs. Wraps one LinkedChain<Song> and delegates to it.
 * Uses default identity equality, so two playlists may share a name (duplicates allowed but not recommended).
 */
public class Playlist {
    private String name;
    private LinkedChain<Song> songs;

    /** Rejects a null or blank name with IllegalArgumentException; starts empty. O(1). */
    public Playlist(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Playlist name cannot be blank");
        }
        this.name = name.trim();
        this.songs = new LinkedChain<>();
    }

    public String getName() {
        return name;
    }

    /** Adds a song (duplicates kept). Null is rejected by the chain. O(1). */
    public void addSong(Song s) {
        songs.add(s);
    }

    /** Removes one copy; false if absent. O(n). */
    public boolean removeSong(Song s) {
        return songs.remove(s);
    }

    /** True if the song is in this playlist. O(n). */
    public boolean containsSong(Song s) {
        return songs.contains(s);
    }

    /** Number of songs, counting duplicates. O(1). */
    public int songCount() {
        return songs.size();
    }

    /** Snapshot of the songs, oldest added first. O(n). */
    public Song[] songsInOrder() {
        return ChainView.inInsertionOrder(songs, new Song[0]);
    }

    @Override
    public String toString() {
        return name + " (" + songs.size() + " songs)";
    }
}
