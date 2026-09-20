package ru.mipt.bit.platformer.model;

// An immutable snapshot. Reading it cannot change the world.
public final class EntityState {
    public final Cell cell;
    public final Cell destination;
    public final Direction facing;
    public final float progress;

    EntityState(Cell cell, Cell destination, Direction facing, float progress) {
        this.cell = cell;
        this.destination = destination;
        this.facing = facing;
        this.progress = progress;
    }
}
