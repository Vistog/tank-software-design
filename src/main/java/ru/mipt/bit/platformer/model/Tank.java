package ru.mipt.bit.platformer.model;

import java.util.Objects;

/** Domain: tank intentions, health and current behavior state. */
public final class Tank extends Entity {
    private Direction requestedDirection;
    private boolean shotRequested;
    private int health;
    private final int maxHealth;
    private TankState state = HealthState.LIGHT;

    public Tank(int id) {
        this(id, 100);
    }

    public Tank(int id, int health) {
        super(id, true);
        if (health <= 0) throw new IllegalArgumentException("Health must be positive");
        this.health = health;
        this.maxHealth = health;
    }

    // This is an intention. Only the world can change the actual position.
    public void move(Direction direction) {
        Objects.requireNonNull(direction);
        if (isAlive()) requestedDirection = direction;
    }

    public void shoot() {
        if (isAlive() && state.canShoot()) shotRequested = true;
    }

    @Override
    public float speedMultiplier() {
        return state.speedMultiplier();
    }

    public int health() {
        return health;
    }

    public float healthFraction() {
        return (float) health / maxHealth;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        if (damage < 0) throw new IllegalArgumentException("Negative damage");
        health = Math.max(0, health - damage);
        state = HealthState.forHealth(health, maxHealth);
        if (!isAlive()) {
            requestedDirection = null;
            shotRequested = false;
        }
    }

    boolean takeShotRequest() {
        boolean requested = shotRequested;
        shotRequested = false;
        return requested && isAlive() && state.canShoot();
    }

    @Override
    protected Direction takeMoveRequest() {
        Direction direction = requestedDirection;
        requestedDirection = null;
        return direction;
    }
}
