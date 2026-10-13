# Melody — Prototype + Tests (Project 2, Deliverable 3)

Melody is a text-menu playlist manager. Every collection in it (songs, playlists, play history) is stored in the **provided generic `LinkedChain<T>`, used unchanged**, and the app classes only call its public methods.

## Build, run, test

**You need:** JDK 11 or newer (`java -version` and `javac -version` must both work), and the JUnit 5 console launcher jar in a `lib` folder (`junit-platform-console-standalone-1.11.4.jar`, from Maven Central; any 1.9+ version works if you change the file name in the commands).

**Windows PowerShell** (run from `project-root`):

```powershell
To run the program:
-cp out Main
To run the tests: 
javac -cp "lib\junit-platform-console-standalone-1.11.4.jar" -d out *.java
java -jar lib\junit-platform-console-standalone-1.11.4.jar execute --class-path out --scan-class-path=out
```

**Expected test result: `96 tests found`, `96 tests successful`, `0 tests failed`.** If you see a different number, see Troubleshooting.

To compile **only the app** (no JUnit jar needed), list the app files instead of `*.java`:
`javac -d out Main.java MelodyApp.java Song.java Playlist.java LinkedChain.java ChainView.java`

**What we verified, and what we couldn't.** We ran exactly these steps (bash form) on Linux with JDK 21 and a JUnit console jar, in a flat folder like the one above: clean build, 96/96 passing, app runs. We could not execute PowerShell here; the PowerShell lines use the same arguments with Windows paths (the Windows `javac` expands `*.java` itself).

### Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `ClassNotFoundException: Main` / `Could not find or load main class Main` | The code was never compiled, or `-cp` doesn't point at the folder holding `Main.class`. | Run step 2 first, from `project-root`. Check that `out\Main.class` exists, then `java -cp out Main`. |
| `package org.junit.jupiter.api does not exist` / `cannot find symbol ... assertTrue` / dozens of errors from `javac *.java` | `*.java` includes the test files, and `javac` can't see JUnit without `-cp`. | Add `-cp "lib\junit-platform-console-standalone-1.11.4.jar"` to the `javac` command (step 2), or compile only the app files (above). |
| Red "import org.junit… cannot be resolved" in VS Code, but `javac` works | VS Code's Java extension doesn't know about `lib\`. | Keep `.vscode/settings.json` with `{"java.project.referencedLibraries": ["lib/**/*.jar"]}`, then Command Palette → **Java: Clean Java Language Server Workspace** → reload. The red marks are editor-only and don't affect the command line. |
| Test count is not 96 (we reproduced 97 with a leftover class) | A stale `out` folder: `--scan-class-path out` also runs old `.class` files from earlier builds. | Delete `out` (step 1) and rebuild. |
| Test count is still not 96 after a clean build | Your test files differ from ours. | Check `(Select-String -Path *Test.java -Pattern '@Test').Count` prints **96** (Linux: `grep -h "@Test" *Test.java \| wc -l`). If not, re-copy the five `*Test.java` files from the zip. |
| `cannot find symbol: isBlank` | JDK older than 11. | Install JDK 11+ (we used 21). |

##  Java files

| File | Job (one each) |
|---|---|
| `LinkedChain.java` | **Provided by staff, unchanged.** Generic storage only. |
| `Song.java` | Immutable record; validates itself; equality = title + artist + duration. |
| `Playlist.java` | Named collection wrapping a `LinkedChain<Song>`; delegates to it. |
| `MelodyApp.java` | Controller: menu, input validation, output. Holds three chains; no storage code. |
| `ChainView.java` | Tiny static helper (new): turns a chain into an insertion-order snapshot array using only `size()` and `toArray()`. |
| `Main.java` | Entry point only. |
| `LinkedChainTest` (22) · `SongTest` (13) · `PlaylistTest` (14) · `MelodyAppTest` (42) · `ChainViewTest` (5) | JUnit 5. |

## Big-O of the app methods: spec estimate vs. what we built

n = songs in the library, k = copies being removed, p = playlists, h = history entries.

| Method | Spec | Actual | Matches? |
|---|---|---|---|
| `MelodyApp` constructor | O(1) | O(1) | Yes |
| `addSong` | O(1) | O(1): `chain.add` inserts at the front | Yes |
| `removeSong` | O(n) | O(n): snapshot O(n) + title scan O(n) + `chain.remove` O(n) | Yes |
| `searchSong` | O(n) | O(n) (snapshot, then one pass; substring cost on short titles ignored) | Yes |
| `listSongs` | O(n) | O(n) | Yes |
| `showCount` | O(1) | O(1) (`size()`) | Yes |
| `shufflePlay` | O(n) | O(n): snapshot + shuffle + O(1) history add per song | Yes |
| `Playlist.addSong` / `songCount` | O(1) | O(1) | Yes |
| `Playlist.removeSong` / `containsSong` | O(n) | O(n) | Yes |
| `removeAllCopies` (added) | not in spec | O(n + k·n) worst case: `count` is O(n), then k calls to `remove(T)` that are each O(n). Cheap when copies sit near the front, quadratic in the worst case | n/a (the provided chain has no `removeAll`) |
| `addSongToPlaylist` (added) | not in spec | O(p + n) | n/a |
| Extra cost vs. spec | none | Every list/search/shuffle makes an O(n) snapshot array (O(n) extra space) | Time matches; space is new |

Provided `LinkedChain` complexities (from its header) vs. our spec: `add` O(1), `remove(T)` O(n), `contains` O(n), `size`/`isEmpty` O(1). The spec's `removeAll`, tail-pointer append and iterator don't exist in the provided class (see Reflection).

## Bad input: where each case is caught and what happens

| Bad input | Caught in (as spec'd) | Response | Test |
|---|---|---|---|
| Blank/null title or artist | `Song` constructor | `IllegalArgumentException`; `MelodyApp.addSong()` prints the reason and re-prompts | `SongTest.blankTitleIsRejected`, `MelodyAppTest.addSongRePromptsOnBlankTitleAndArtist` |
| Negative / zero duration | `Song` constructor | same | `addSongRePromptsOnNegativeDurationInsteadOfCrashing` |
| Non-numeric / overflowing duration | `MelodyApp.addSong(String,String,String)` (catches `NumberFormatException`) | message + re-prompt | `addSongRePromptsOnNonNumericDuration`, `addSongFromTextRejectsEveryBadCaseWithoutThrowing` |
| Remove an item not in the chain | `LinkedChain.remove(T)` | returns `false`; app prints "Not found" | `LinkedChainTest.removeMissingReturnsFalseAndSizeUnchanged`, `MelodyAppTest.removeSongMissingTitleSaysNotFoundWithoutCrashing` |
| Remove from an empty chain | `LinkedChain.remove(T)` | returns `false` | `newChainSurvivesRemoveCountAndToArray`, `removeSongFromEmptyLibraryAndBlankTitleAreSafe` |
| Huge number of duplicates | `LinkedChain.add` (nothing special needed) | size and counts stay accurate | `largeNumberOfDuplicatesStaysAccurate` (50,000), `thousandsOfDuplicatesAreCountedAndRemovedCorrectly` |
| `null` added to a chain | `LinkedChain.add` (provided guard) | `IllegalArgumentException`; chain unchanged | `addNullIsRejectedAndChainUnchanged`, `PlaylistTest.addNullSongIsRejected` |
| Wrong type given to a chain | Java compiler (generics) | does not compile | n/a |
| Chain changed while being walked | see Reflection: the app only walks snapshot arrays | cannot occur; no exception needed | `ChainViewTest.snapshotIsIndependentOfLaterChainChanges` |
| Invalid menu choice / input ends mid-prompt | `MelodyApp.run` / `addSong()` | "Invalid choice" / "Input ended"; no crash, no infinite loop | `menuSurvivesInvalidChoicesAndEndOfInput`, `addSongStopsCleanlyWhenInputEndsMidEntry` |
| Blank playlist name; null `Scanner` | `Playlist` / `MelodyApp` constructors | `IllegalArgumentException` | `nullOrBlankNameIsRejected`, `nullScannerIsRejectedImmediately` |

## Reflection

### What changed from our specification?

Most changes trace to one fact: our spec described a chain with a tail pointer, `removeAll`, and a fail-fast iterator, but the provided `LinkedChain` has none of those, and `add()` inserts at the **front**.

1. **Insertion order without a tail pointer.** Spec: `add` appends at the tail in O(1). Provided: `add` is O(1) at the *front* and `addToEnd` is O(n). To keep `addSong` at the spec's O(1) we use `add()` and read the chain back through the new `ChainView.inInsertionOrder`, which reverses the `toArray` result. `listSongs`, `searchSong`, playlists and history all show oldest-first as the spec's test plan requires.
2. **No iterator, so no fail-fast `ConcurrentModificationException`.** Spec: `LinkedChain.iterator()` throws on mid-loop changes, with a test. Provided: no iterator. The app never walks a live chain: it copies to an array with `toArray` and loops over that snapshot, so the failure case cannot occur. The spec's iterator tests became `ChainViewTest.snapshotIsIndependentOfLaterChainChanges`.
3. **`removeAll` moved into the app.** Spec: `LinkedChain.removeAll(item)` in a single pass. Provided: none. `MelodyApp.removeAllCopies` calls `count(found)` and then `remove(found)` that many times. Cost: O(n·k) worst case instead of O(n) (see Big-O table).
4. **`Node` is no longer our class.** Spec listed `Node<T>` as its own class; in the provided file it is a private nested class, so we deleted our `Node.java`.
5. **New class `ChainView`** (see item 1). It holds no storage and calls only `size()` and `toArray()`.
6. **`Playlist` exposes `songsInOrder()`** instead of being iterable, so `listPlaylists` can print a playlist's songs without touching its chain. Playlists keep identity equality so two can share a name, as the scope allowed.
7. **`MelodyApp` has a second constructor** `(Scanner, PrintStream, Random)` so tests can capture output and seed the shuffle. The spec's one-argument constructor still exists and delegates to it. Any null argument throws `IllegalArgumentException`.
8. **`addSong` became two methods.** Spec: one `void addSong(...)`. Built: `void addSong()` (interactive, re-prompts until valid or input ends) and `boolean addSong(String, String, String)` (one validated attempt, returns success). The loop needs a success signal and tests need to feed raw text.
9. **`Song` trims its strings and has `toString`**, neither in the spec. Without trimming, `"Imagine "` and `"Imagine"` would be different songs.
10. **A third element type and extra methods.** Spec named `Song` and `Playlist`; we also store `String` titles (play history) in a `LinkedChain<String>`. Added `removeAllCopies`, `showHistory`, `buildShuffleOrder`, `createPlaylist`, `addSongToPlaylist`, `listPlaylists`, `run`.
11. **Behavior the spec left open.** `searchSong` is a case-insensitive *substring* match. `removeSong`/`removeAllCopies` match titles case-insensitively and exactly, then call `remove(T)`, which removes the **most recently added** equal copy because `add()` inserts at the front (`removeSongRemovesTheMostRecentCopyAndKeepsOtherSongsInOrder` pins this down).
12. **Tests.** The spec's LinkedChain test-plan rows (`removeAll`, `iterator`) no longer apply; `LinkedChainTest` instead tests the provided class's behavior as the app uses it (`count`, `toArray`, `addToEnd`, front-insertion order, null rejection). The spec planned one normal and one bad-input case per method; we wrote 96 tests, adding tail-of-chain removal, 50,000 duplicates, `null` arguments, and input that ends mid-prompt.

### What changed in the LinkedChain?

**Nothing: we used the provided `LinkedChain` unchanged except for some changes in indentation for personal readability.** The app calls `add`, `remove(T)`, `count`, `contains`, `size`, `isEmpty` and `toArray` only. We did not need `addToEnd`, `get`, `clear` or `remove()`; `LinkedChainTest` still covers them.

### What happened to our unknowns?

| Unknown from scope | Outcome |
|---|---|
| Tail pointer for O(1) append? | **Resolved differently.** We never needed one: `add()` already inserts at the front in O(1), and `ChainView` restores insertion order on read. **Still open:** if reads became much more frequent than adds, an O(n) snapshot per read might be worth replacing with a tail-pointer or iterator extension of `LinkedChain`; we haven't measured it. |
| `remove()` one vs. all duplicates? | **Resolved.** `remove(T)` takes one (the front-most); "all copies" is `count` + repeated `remove(T)` in `removeAllCopies`, tested. **Open:** it is O(n·k) in the worst case; a single-pass `removeAll` added to the chain would fix that. |
| `equals()` for `Song`? | **Resolved:** title + artist + duration, exact and case-sensitive after trimming. **Still open:** whether `"imagine"` and `"Imagine"` should be the same song; title *search* is case-insensitive but song *identity* is not. |
| Modification during iteration? | **Resolved by avoidance:** the app only loops over snapshot arrays, never a live chain, so there is nothing to modify mid-iteration. |

### What doesn't work yet?

- **Save/load to a file** (a scoped stretch goal) is not built.
- **Playlists can't remove songs from the menu.** `Playlist.removeSong` exists and is tested, but no menu option calls it.
- **Playlists share `Song` objects with the library.** Removing a song from the library does not remove it from playlists.
- **An invalid entry restarts all three prompts** instead of re-asking only the bad field, and a blank title plus a blank duration reports the duration error first (duration is parsed before the `Song` is built).
- **No upper limit on duration**: any positive `int` is accepted.
- **`Main.java` has no JUnit test.** It is three lines delegating to `MelodyApp.run()`, which is tested; we ran `Main` by hand with scripted input.
- No tests are failing or skipped.

## Next Steps

### 1. Finish the MVP (in this order)

The MVP path already works, so what remains is hardening:

| # | Item | Owner |
|---|---|---|
| 1 | Swap in the original Canvas `LinkedChain.java`, re-run all 96 tests, and fix the Reflection if anything differs | Hannah |
| 2 | Re-prompt only the invalid field in `MelodyApp.addSong()`; report title/artist errors before the duration error | Monya |
| 3 | Choose a maximum duration and enforce it in `Song` | Monya |
| 4 | Manual end-to-end pass on a real console (not scripted input); fix whatever turns up | Matias |

### 2. Stretch goals

| Stretch goal | Decision |
|---|---|
| Shuffle without mutating the library | **Done** |
| Duplicate-aware removal (one vs. all copies) | **Done** (Member A may add a single-pass `removeAll` to `LinkedChain` if the O(n·k) cost matters) |
| Nested playlists | **Done (basic).** Committed next: remove-from-playlist menu option and shuffle within a playlist (Matias) |
| Save/load to a file | **Committed** (Hannah) |
| Simple GUI | **Dropped.** It adds an interface, not capability |

### 3. New tests needed

- **Save/load:** missing file, empty file, corrupted line, a title containing the delimiter, and a round trip (save then load gives equal songs with duplicates preserved).
- **Playlist menu:** remove a song that isn't in the playlist, remove from an empty playlist, shuffle an empty playlist.
- **`Song` maximum duration:** values just under, at, and over the limit.
- **Field-level re-prompt in `addSong()`:** only the bad field is asked again, and input that ends mid-field still exits cleanly.
- **Playlist/library interaction:** what a playlist shows after its song is removed from the library, once we decide the behavior.

### 4. Who does what

Hannah: item 1 and save/load. Monya: items 2 and 3. Matias: item 4 and the playlist menu work.

## Attribution


| File | Who | How |
|---|---|---|
| `LinkedChain.java` | N/A | Provided; **not modified** by us; Hannah reviewed and tested against it |
| `ChainView.java` | Hannah | Wrote and tested with GenAI proofreading|
| `Song.java` | Monya | Wrote and tested with GenAI proofreading |
| `Playlist.java` | Monya | Wrote and tested with GenAI proofreading |
| `MelodyApp.java` | Matias | Wrote and tested with GenAI proofreading |
| `Main.java` | Matias | Wrote and tested with GenAI proofreading |
| `LinkedChainTest.java` | Hannah | Wrote and used GenAI for edge test cases |
| `ChainViewTest.java` | Hannah | Wrote and used GenAI for edge test cases |
| `SongTest.java` | Monya | Wrote and used GenAI for edge test cases |
| `PlaylistTest.java` | Monya | Wrote and used GenAI for edge test cases |
| `MelodyAppTest.java` | Matias | Wrote and used GenAI for edge test cases |
| `README.md` | All | Drafted with GenAI; edited by team |