package ru.mipt.bit.platformer.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GameWorld {
    private final Field field;
    private final float movementDuration;
    private final Map<Integer, Placement> placements = new LinkedHashMap<>();

    public GameWorld(Field field, float movementDuration) {
        this.field = Objects.requireNonNull(field);
        if (!Float.isFinite(movementDuration) || movementDuration <= 0) {
            throw new IllegalArgumentException("Movement duration must be finite and positive");
        }
        this.movementDuration = movementDuration;
    }

    public void add(Entity entity, Cell cell, Direction facing) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(cell);
        Objects.requireNonNull(facing);
        if (placements.containsKey(entity.id) || !field.contains(cell)) {
            throw new IllegalArgumentException("Duplicate id or position outside the field");
        }
        if (entity.blocksMovement && !isFreeForMovement(cell)) {
            throw new IllegalArgumentException("Cell is occupied");
        }
        placements.put(entity.id, new Placement(entity, cell, facing));
    }

    public EntityState getState(int entityId) {
        Placement placement = placements.get(entityId);
        if (placement == null) {
            throw new IllegalArgumentException("Unknown entity: " + entityId);
        }
        return new EntityState(placement.cell, placement.destination,
                placement.facing, placement.progress);
    }

    public boolean isFreeForMovement(Cell cell) {
        if (!field.contains(cell)) return false;
        for (Placement placement : placements.values()) {
            if (placement.entity.blocksMovement &&
                    (placement.cell.equals(cell) || placement.destination.equals(cell))) {
                return false;
            }
        }
        return true;
    }

    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        // Requests are handled in insertion order. The first one reserves the cell.
        for (Placement placement : placements.values()) {
            Direction direction = placement.entity.takeMoveRequest();
            if (direction != null && placement.progress == 1f) {
                tryStartMove(placement, direction);
            }
        }
        // Keep source cells occupied until all requests have been handled.
        for (Placement placement : placements.values()) {
            placement.advance(deltaTime / movementDuration);
        }
    }

    private void tryStartMove(Placement placement, Direction direction) {
        placement.facing = direction;
        Cell destination = direction.nextCell(placement.cell);
        if (isFreeForMovement(destination)) {
            placement.destination = destination;
            placement.progress = 0;
        }
    }

    private static final class Placement {
        private final Entity entity;
        private Cell cell;
        private Cell destination;
        private Direction facing;
        private float progress = 1f;

        private Placement(Entity entity, Cell cell, Direction facing) {
            this.entity = entity;
            this.cell = cell;
            this.destination = cell;
            this.facing = facing;
        }

        private void advance(float step) {
            progress = Math.min(1f, progress + step);
            if (progress == 1f) cell = destination;
        }
    }
}
