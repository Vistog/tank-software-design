package ru.mipt.bit.platformer.level;

import ru.mipt.bit.platformer.model.Cell;
import ru.mipt.bit.platformer.model.GameWorld;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class RandomTankSpawner {
    private final Random random;

    public RandomTankSpawner(Random random) {
        this.random = random;
    }

    public List<Cell> positions(GameWorld world, int width, int height, int count) {
        if (width <= 0 || height <= 0 || count < 0) {
            throw new IllegalArgumentException("Invalid field size or tank count");
        }
        List<Cell> free = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = new Cell(x, y);
                if (world.isFreeForMovement(cell)) free.add(cell);
            }
        }
        Collections.shuffle(free, random);
        return new ArrayList<>(free.subList(0, Math.min(count, free.size())));
    }
}
