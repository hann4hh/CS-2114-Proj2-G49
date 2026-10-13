/** Song record. Validates its own fields and defines equality (all three fields). */
public class Song {
    private String title;
    private String artist;
    private int duration;   // seconds, > 0

    /** Rejects null/blank title or artist and duration <= 0 with IllegalArgumentException. O(1). */
    public Song(String title, String artist, int duration) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (artist == null || artist.isBlank()) {
            throw new IllegalArgumentException("Artist cannot be blank");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0 seconds");
        }
        this.title = title.trim();
        this.artist = artist.trim();
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }

    /** Equal only if title, artist, and duration all match exactly. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Song)) {
            return false;   // also covers null: instanceof is false for null
        }
        Song other = (Song) o;
        return duration == other.duration
                && title.equals(other.title)
                && artist.equals(other.artist);
    }

    /** Consistent with equals. */
    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + duration;
        return result;
    }

    @Override
    public String toString() {
        return title + " - " + artist + " (" + (duration / 60) + ":" + String.format("%02d", duration % 60) + ")";
    }
}
