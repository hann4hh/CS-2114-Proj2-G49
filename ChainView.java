import java.util.Arrays;

/**
 * App-side helper (no storage). LinkedChain.add() inserts at the FRONT, so toArray() returns
 * newest-first. This reads a chain into an array in insertion order using only toArray/size.
 */
final class ChainView {
    private ChainView() {
    }

    /**
     * Returns a snapshot of the chain, oldest entry first. typeHint is any array of the element
     * type (e.g. new Song[0]); it only fixes the runtime array type. O(n) time and space.
     * The snapshot is independent of later chain changes.
     */
    static <T> T[] inInsertionOrder(LinkedChain<T> chain, T[] typeHint) {
        T[] copy = Arrays.copyOf(typeHint, chain.size());
        chain.toArray(copy);   // newest first
        for (int i = 0, j = copy.length - 1; i < j; i++, j--) {
            T tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }
}
