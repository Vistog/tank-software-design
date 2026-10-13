package ru.mipt.bit.platformer.level;

import ru.mipt.bit.platformer.model.Cell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class RandomLevelSource implements LevelSource {
    private final int width;
    private final int height;
    private final int treeCount;
    private final Random random;

    public RandomLevelSource(int width, int height, int treeCount, Random random) {
        if (width <= 0 || height <= 0 || treeCount < 0 || treeCount >= (long) width * height) {
            throw new IllegalArgumentException("Leave at least one cell for the player");
        }
        this.width = width;
        this.height = height;
        this.treeCount = treeCount;
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public LevelData load() {
        List<Cell> cells = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) cells.add(new Cell(x, y));
        }
        Collections.shuffle(cells, random);
        return new LevelData(width, height, cells.get(0), cells.subList(1, treeCount + 1));
    }
}
