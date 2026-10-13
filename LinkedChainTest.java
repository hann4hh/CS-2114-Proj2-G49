import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Contract tests for the PROVIDED LinkedChain, covering every method the app relies on
 * (add, remove(T), count, contains, size, isEmpty, toArray) plus the rest of its public API.
 */
class LinkedChainTest {
    private static final Song IMAGINE = new Song("Imagine", "John Lennon", 183);
    private static final Song YESTERDAY = new Song("Yesterday", "The Beatles", 125);
    private static final Song HEY_JUDE = new Song("Hey Jude", "The Beatles", 431);

    // ---- constructor
    @Test
    public void constructorCreatesEmptyChain() {
        LinkedChain<Song> chain = new LinkedChain<>();
        assertEquals(0, chain.size());
        assertTrue(chain.isEmpty());
        assertEquals("[]", chain.toString());
    }

    @Test
    public void newChainSurvivesRemoveCountAndToArray() {   // bad input: nothing to act on
        LinkedChain<Song> chain = new LinkedChain<>();
        assertFalse(chain.remove(IMAGINE));
        assertNull(chain.remove());
        assertEquals(0, chain.count(IMAGINE));
        assertEquals(0, chain.toArray(new Song[0]).length);
    }

    // ---- add / addToEnd
    @Test
    public void addIncreasesSizeAndContainsFindsIt() {
        LinkedChain<Song> chain = new LinkedChain<>();
        assertTrue(chain.add(IMAGINE));
        assertEquals(1, chain.size());
        assertTrue(chain.contains(IMAGINE));
    }

    @Test
    public void addNullIsRejectedAndChainUnchanged() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        assertThrows(IllegalArgumentException.class, () -> chain.add(null));
        assertThrows(IllegalArgumentException.class, () -> chain.addToEnd(null));
        assertEquals(1, chain.size());
    }

    @Test
    public void addPutsNewestFirstAndAddToEndKeepsInsertionOrder() {
        LinkedChain<String> front = new LinkedChain<>();
        front.add("a");
        front.add("b");
        front.add("c");
        assertArrayEquals(new String[] {"c", "b", "a"}, front.toArray(new String[3]));
        LinkedChain<String> end = new LinkedChain<>();
        end.addToEnd("a");
        end.addToEnd("b");
        end.addToEnd("c");
        assertArrayEquals(new String[] {"a", "b", "c"}, end.toArray(new String[3]));
    }

    @Test
    public void duplicatesAreKept() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        chain.add(IMAGINE);
        assertEquals(2, chain.size());
        assertEquals(2, chain.count(IMAGINE));
    }

    @Test
    public void largeNumberOfDuplicatesStaysAccurate() {   // adversarial volume case
        LinkedChain<Song> chain = new LinkedChain<>();
        for (int i = 0; i < 50_000; i++) {
            chain.add(IMAGINE);
        }
        assertEquals(50_000, chain.size());
        assertEquals(50_000, chain.count(IMAGINE));
        assertEquals(50_000, chain.toArray(new Song[50_000]).length);
    }

    // ---- remove
    @Test
    public void removeExistingReturnsTrueAndShrinks() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        chain.add(YESTERDAY);
        assertTrue(chain.remove(IMAGINE));
        assertEquals(1, chain.size());
        assertFalse(chain.contains(IMAGINE));
    }

    @Test
    public void removeMissingReturnsFalseAndSizeUnchanged() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        assertFalse(chain.remove(YESTERDAY));
        assertEquals(1, chain.size());
    }

    @Test
    public void removeNullReturnsFalse() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        assertFalse(chain.remove(null));
        assertEquals(1, chain.size());
    }

    @Test
    public void removeTakesOnlyOneDuplicateTheFirstFromTheFront() {
        LinkedChain<String> chain = new LinkedChain<>();
        chain.add("x");   // oldest
        chain.add("y");
        chain.add("x");   // newest, at the front
        assertTrue(chain.remove("x"));
        assertArrayEquals(new String[] {"y", "x"}, chain.toArray(new String[2]));   // newest copy went
    }

    @Test
    public void removeHeadMiddleAndTailKeepLinksCorrect() {
        LinkedChain<String> chain = new LinkedChain<>();
        for (String s : new String[] {"a", "b", "c", "d"}) {
            chain.addToEnd(s);
        }
        assertTrue(chain.remove("a"));   // head
        assertTrue(chain.remove("c"));   // middle
        assertTrue(chain.remove("d"));   // tail
        assertArrayEquals(new String[] {"b"}, chain.toArray(new String[1]));
        chain.add("e");
        assertEquals("[e, b]", chain.toString());
    }

    @Test
    public void removeNoArgTakesTheFirstEntry() {
        LinkedChain<String> chain = new LinkedChain<>();
        chain.add("a");
        chain.add("b");
        assertEquals("b", chain.remove());
        assertEquals(1, chain.size());
    }

    // ---- count / contains
    @Test
    public void countReturnsNumberOfCopies() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        chain.add(YESTERDAY);
        chain.add(IMAGINE);
        assertEquals(2, chain.count(IMAGINE));
        assertEquals(0, chain.count(HEY_JUDE));
        assertEquals(0, chain.count(null));
    }

    @Test
    public void containsFindsAddedSongByEqualityNotIdentity() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        assertTrue(chain.contains(new Song("Imagine", "John Lennon", 183)));
    }

    @Test
    public void containsOnEmptyChainAndNullIsFalse() {
        assertFalse(new LinkedChain<Song>().contains(IMAGINE));
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        assertFalse(chain.contains(null));
    }

    // ---- size / isEmpty
    @Test
    public void sizeCountsThreeSongsAndIsZeroWhenEmpty() {
        LinkedChain<Song> chain = new LinkedChain<>();
        assertEquals(0, chain.size());
        chain.add(IMAGINE);
        chain.add(YESTERDAY);
        chain.add(HEY_JUDE);
        assertEquals(3, chain.size());
    }

    @Test
    public void isEmptyTrueForNewChainFalseWithOneItem() {
        LinkedChain<Song> chain = new LinkedChain<>();
        assertTrue(chain.isEmpty());
        chain.add(IMAGINE);
        assertFalse(chain.isEmpty());
    }

    // ---- toArray / get / clear
    @Test
    public void toArrayRejectsNullAndTooSmallArrays() {
        LinkedChain<Song> chain = new LinkedChain<>();
        chain.add(IMAGINE);
        assertThrows(IllegalArgumentException.class, () -> chain.toArray(null));
        assertThrows(IllegalArgumentException.class, () -> chain.toArray(new Song[0]));
    }

    @Test
    public void getReturnsEntryAndRejectsBadIndex() {
        LinkedChain<String> chain = new LinkedChain<>();
        chain.addToEnd("a");
        chain.addToEnd("b");
        assertEquals("b", chain.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(2));
    }

    @Test
    public void clearEmptiesTheChain() {
        LinkedChain<String> chain = new LinkedChain<>();
        chain.add("a");
        chain.clear();
        assertTrue(chain.isEmpty());
        assertEquals(0, chain.size());
    }

    // ---- reuse: the SAME class stores unrelated element types
    @Test
    public void sameClassHoldsDifferentElementTypes() {
        LinkedChain<Song> songs = new LinkedChain<>();
        LinkedChain<Playlist> lists = new LinkedChain<>();
        LinkedChain<String> titles = new LinkedChain<>();
        LinkedChain<Integer> numbers = new LinkedChain<>();
        songs.add(IMAGINE);
        lists.add(new Playlist("Workout"));
        titles.add("Imagine");
        numbers.add(7);
        assertEquals(1, songs.size());
        assertEquals(1, lists.size());
        assertEquals(1, titles.size());
        assertEquals(1, numbers.size());
        assertTrue(songs.contains(IMAGINE));
        assertTrue(titles.contains("Imagine"));
        assertTrue(numbers.contains(7));
    }
}
