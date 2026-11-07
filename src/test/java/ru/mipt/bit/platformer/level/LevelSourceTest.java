package ru.mipt.bit.platformer.level;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.mipt.bit.platformer.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static org.junit.Assert.*;

public class LevelSourceTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    private LevelData read(String text) throws IOException {
        Path path = folder.newFile().toPath();
        Files.writeString(path, text);
        return new FileLevelSource(path).load();
    }

    @Test public void fileCoordinatesMatchTopToBottomRows() throws IOException {
        LevelData level = read("T__\n_XT\n");
        assertEquals(3, level.width);
        assertEquals(2, level.height);
        assertEquals(new Cell(1, 0), level.player);
        assertEquals(Arrays.asList(new Cell(0, 1), new Cell(2, 0)), level.trees);
    }

    @Test public void windowsLineEndingsAndSingleCellWork() throws IOException {
        assertEquals(new Cell(0, 1), read("X_\r\n_T\r\n").player);
        assertTrue(read("X").trees.isEmpty());
    }

    @Test public void invalidFilesAreRejected() {
        for (String text : Arrays.asList("", "\n", "__", "XX", "X_\n_", "X?", "X ", "X\n\n")) {
            assertThrows(IllegalArgumentException.class, () -> read(text));
        }
    }

    @Test public void missingFileIsReported() {
        Path path = folder.getRoot().toPath().resolve("missing.txt");
        assertThrows(IOException.class, () -> new FileLevelSource(path).load());
    }

    @Test public void generationHasExactCountAndNoOverlaps() {
        for (int seed = 0; seed < 100; seed++) {
            LevelData level = new RandomLevelSource(7, 4, 12, new Random(seed)).load();
            assertEquals(7, level.width);
            assertEquals(4, level.height);
            assertEquals(12, level.trees.size());
            assertEquals(12, new HashSet<>(level.trees).size());
            assertFalse(level.trees.contains(level.player));
            RectangleField field = new RectangleField(7, 4);
            assertTrue(field.contains(level.player));
            for (Cell tree : level.trees) assertTrue(field.contains(tree));
        }
    }

    @Test public void generationIsReproducibleWithSameSeed() {
        LevelData first = new RandomLevelSource(6, 3, 8, new Random(42)).load();
        LevelData second = new RandomLevelSource(6, 3, 8, new Random(42)).load();
        assertEquals(first.player, second.player);
        assertEquals(first.trees, second.trees);
    }

    @Test public void emptyAndAlmostFullMapsWork() {
        assertTrue(new RandomLevelSource(1, 1, 0, new Random(1)).load().trees.isEmpty());
        assertEquals(5, new RandomLevelSource(3, 2, 5, new Random(1)).load().trees.size());
    }

    @Test public void invalidGeneratorParametersAreRejected() {
        int[][] values = {{0, 1, 0}, {1, -1, 0}, {2, 2, -1}, {2, 2, 4}};
        for (int[] value : values) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RandomLevelSource(value[0], value[1], value[2], new Random()));
        }
    }

    @Test public void dataCopiesTreeList() {
        List<Cell> trees = new ArrayList<>();
        trees.add(new Cell(1, 0));
        LevelData data = new LevelData(2, 1, new Cell(0, 0), trees);
        trees.clear();
        assertEquals(1, data.trees.size());
        assertThrows(UnsupportedOperationException.class, () -> data.trees.clear());
    }

    @Test public void invalidPlacementsAreRejected() {
        Cell player = new Cell(0, 0);
        assertThrows(IllegalArgumentException.class,
                () -> new LevelData(2, 2, player, Arrays.asList(player)));
        assertThrows(IllegalArgumentException.class,
                () -> new LevelData(2, 2, player, Arrays.asList(new Cell(2, 0))));
        assertThrows(IllegalArgumentException.class,
                () -> new LevelData(2, 2, player, Arrays.asList(new Cell(1, 0), new Cell(1, 0))));
        assertThrows(IllegalArgumentException.class,
                () -> new LevelData(2, 2, new Cell(-1, 0), Collections.emptyList()));
    }

    @Test public void bothSourcesPopulateWorldWithoutSpecialCases() throws IOException {
        Path path = folder.newFile().toPath();
        Files.writeString(path, "X_T\n___");
        for (LevelSource source : Arrays.asList(new FileLevelSource(path),
                new RandomLevelSource(3, 2, 2, new Random(3)))) {
            LevelData data = source.load();
            GameWorld world = new GameWorld(new RectangleField(data.width, data.height), 0.4f);
            world.add(new Tank(1), data.player, Direction.RIGHT);
            int id = 2;
            for (Cell tree : data.trees) world.add(new Tree(id++), tree, Direction.RIGHT);
            assertEquals(data.player, world.getState(1).cell);
            for (Cell tree : data.trees) assertFalse(world.isFreeForMovement(tree));
        }
    }
}
