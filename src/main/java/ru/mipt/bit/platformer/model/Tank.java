package ru.mipt.bit.platformer.model;

import java.util.Objects;

public final class Tank extends Entity {
    private Direction requestedDirection;
    private boolean shotRequested;
    private int health;

    public Tank(int id) {
        this(id, 100);
    }

    public Tank(int id, int health) {
        super(id, true);
        if (health <= 0) throw new IllegalArgumentException("Health must be positive");
        this.health = health;
    }

    // This is an intention. Only the world can change the actual position.
    public void move(Direction direction) {
        Objects.requireNonNull(direction);
        if (isAlive()) requestedDirection = direction;
    }

    public void shoot() {
        if (isAlive()) shotRequested = true;
    }

    public int health() {
        return health;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        if (damage < 0) throw new IllegalArgumentException("Negative damage");
        health = Math.max(0, health - damage);
        if (!isAlive()) {
            requestedDirection = null;
            shotRequested = false;
        }
    }

    boolean takeShotRequest() {
        boolean requested = shotRequested;
        shotRequested = false;
        return requested;
    }

    @Override
    protected Direction takeMoveRequest() {
        Direction direction = requestedDirection;
        requestedDirection = null;
        return direction;
    }
}
