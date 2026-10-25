package ru.mipt.bit.platformer.model;

public final class RectangleField implements Field {
    private final int width;
    private final int height;

    public RectangleField(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Field dimensions must be positive");
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean contains(Cell cell) {
        return cell.x >= 0 && cell.x < width && cell.y >= 0 && cell.y < height;
    }
}
