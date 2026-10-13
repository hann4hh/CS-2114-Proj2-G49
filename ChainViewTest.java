import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests ChainView.inInsertionOrder: snapshot of a chain, oldest first. */
class ChainViewTest {
    @Test void returnsEntriesOldestFirst() {
        LinkedChain<String> chain = new LinkedChain<>();
        chain.add("first");
        chain.add("second");
        chain.add("third");
        assertArrayEquals(new String[] {"first", "second", "third"},
                ChainView.inInsertionOrder(chain, new String[0]));
    }

    @Test void emptyChainGivesEmptyArray() {
        assertEquals(0, ChainView.inInsertionOrder(new LinkedChain<Song>(), new Song[0]).length);
    }

    @Test void singleEntryAndEvenAndOddLengthsReverseCorrectly() {
        LinkedChain<Integer> chain = new LinkedChain<>();
        chain.add(1);
        assertArrayEquals(new Integer[] {1}, ChainView.inInsertionOrder(chain, new Integer[0]));
        chain.add(2);
        assertArrayEquals(new Integer[] {1, 2}, ChainView.inInsertionOrder(chain, new Integer[0]));
        chain.add(3);
        assertArrayEquals(new Integer[] {1, 2, 3}, ChainView.inInsertionOrder(chain, new Integer[0]));
    }

    @Test void snapshotIsIndependentOfLaterChainChanges() {   // why the app never sees concurrent modification
        LinkedChain<String> chain = new LinkedChain<>();
        chain.add("a");
        chain.add("b");
        String[] snap = ChainView.inInsertionOrder(chain, new String[0]);
        chain.add("c");
        chain.remove("a");
        assertArrayEquals(new String[] {"a", "b"}, snap);
        for (String s : snap) {          // changing the chain while walking the snapshot is safe
            chain.remove(s);
        }
        assertTrue(chain.isEmpty() || chain.size() == 1);
    }

    @Test void worksForEveryElementTypeAndDoesNotChangeTheChain() {
        LinkedChain<Song> songs = new LinkedChain<>();
        songs.add(new Song("A", "x", 1));
        songs.add(new Song("B", "x", 2));
        Song[] snap = ChainView.inInsertionOrder(songs, new Song[0]);
        assertEquals("A", snap[0].getTitle());
        assertEquals(2, songs.size());
        LinkedChain<Playlist> lists = new LinkedChain<>();
        lists.add(new Playlist("One"));
        lists.add(new Playlist("Two"));
        assertEquals("One", ChainView.inInsertionOrder(lists, new Playlist[0])[0].getName());
    }
}
