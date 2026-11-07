package ru.mipt.bit.platformer.level;

import ru.mipt.bit.platformer.model.Cell;
import ru.mipt.bit.platformer.model.RectangleField;
import java.util.HashSet;
import java.util.List;

public final class LevelData {
    public final int width;
    public final int height;
    public final Cell player;
    public final List<Cell> trees;

    public LevelData(int width, int height, Cell player, List<Cell> trees) {
        RectangleField field = new RectangleField(width, height);
        if (player == null || !field.contains(player)) {
            throw new IllegalArgumentException("Player must be inside the field");
        }
        this.trees = List.copyOf(trees);
        if (new HashSet<>(trees).size() != trees.size()) {
            throw new IllegalArgumentException("Duplicate trees");
        }
        for (Cell tree : trees) {
            if (!field.contains(tree) || tree.equals(player)) {
                throw new IllegalArgumentException("Tree outside field or on player");
            }
        }
        this.width = width;
        this.height = height;
        this.player = player;
    }
}
