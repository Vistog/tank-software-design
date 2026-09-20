package ru.mipt.bit.platformer.model;

import java.util.Objects;

public final class Tank extends Entity {
    private Direction requestedDirection;

    public Tank(int id) {
        super(id, true);
    }

    // This is an intention. Only the world can change the actual position.
    public void move(Direction direction) {
        requestedDirection = Objects.requireNonNull(direction);
    }

    @Override
    protected Direction takeMoveRequest() {
        Direction direction = requestedDirection;
        requestedDirection = null;
        return direction;
    }
}
