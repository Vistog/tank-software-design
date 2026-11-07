package ru.mipt.bit.platformer.level;

import ru.mipt.bit.platformer.model.Cell;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileLevelSource implements LevelSource {
    private final Path path;

    public FileLevelSource(Path path) {
        this.path = path;
    }

    @Override
    public LevelData load() throws IOException {
        List<String> rows = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            throw new IllegalArgumentException("Level is empty");
        }
        int width = rows.get(0).length();
        Cell player = null;
        List<Cell> trees = new ArrayList<>();
        for (int row = 0; row < rows.size(); row++) {
            if (rows.get(row).length() != width) {
                throw new IllegalArgumentException("Rows must have equal width");
            }
            for (int x = 0; x < width; x++) {
                // First line is the top of the map; world Y grows upwards.
                Cell cell = new Cell(x, rows.size() - row - 1);
                switch (rows.get(row).charAt(x)) {
                    case 'T': trees.add(cell); break;
                    case 'X':
                        if (player != null) throw new IllegalArgumentException("Multiple players");
                        player = cell;
                        break;
                    case '_': break;
                    default: throw new IllegalArgumentException("Use only T, X and _");
                }
            }
        }
        return new LevelData(width, rows.size(), player, trees);
    }
}
