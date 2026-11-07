package ru.mipt.bit.platformer.model;

public enum Direction {
    UP(0, 1, 90f), LEFT(-1, 0, 180f), DOWN(0, -1, -90f), RIGHT(1, 0, 0f);

    public final int dx;
    public final int dy;
    public final float rotationDegrees;

    Direction(int dx, int dy, float rotationDegrees) {
        this.dx = dx;
        this.dy = dy;
        this.rotationDegrees = rotationDegrees;
    }

    public Cell nextCell(Cell cell) {
        return new Cell(cell.x + dx, cell.y + dy);
    }
}
